package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.common.io.Serializable;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.events.CommunicatorDisconnectedEvent;
import com.gamelibrary2d.network.common.events.CommunicatorDisconnectedListener;
import com.gamelibrary2d.network.common.initialization.*;
import com.gamelibrary2d.network.common.initialization.ConditionalInitializationTask;
import com.gamelibrary2d.network.common.initialization.InitializationTask;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServer implements Server {
    private final Factory<Integer> communicatorIdFactory = RandomInstance.get()::nextInt;
    private final List<PendingCommunicator> pendingCommunicators;
    private final DataBuffer outgoingBuffer;
    private final DataBuffer incomingBuffer;
    private final List<Communicator> communicators;
    private final DelayedMonitor delayedMonitor = new DelayedMonitor();
    private final CommunicatorDisconnectedListener disconnectedEventListener = this::onDisconnectedEvent;
    private final AtomicBoolean running = new AtomicBoolean();

    protected AbstractServer() {
        incomingBuffer = new DynamicByteBuffer();
        incomingBuffer.flip();
        outgoingBuffer = new DynamicByteBuffer();
        communicators = new ArrayList<>();
        pendingCommunicators = new ArrayList<>();
    }

    protected void addPendingCommunicator(Communicator communicator) {
        InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
        initializer.addProducer((__, com) -> writeIdentifier(com, communicatorIdFactory));
        initializer.addProducer(this::connectedTask);
        communicator.configureAuthentication(initializer);
        initializer.addProducer(this::authenticatedTask);
        initializeClient(initializer);

        pendingCommunicators.add(new PendingCommunicator(
                communicator,
                new CommunicatorInitializationContext(),
                initializer));

        communicator.addDisconnectedListener(disconnectedEventListener);
    }

    private void writeIdentifier(Communicator communicator, Factory<Integer> idFactory) {
        int id = idFactory.create();
        communicator.setId(id);
        communicator.getOutgoing().putInt(id);
    }

    private void connectedTask(CommunicatorInitializationContext context, Communicator communicator) {
        onConnected(communicator);
    }

    private void authenticatedTask(CommunicatorInitializationContext context, Communicator communicator) {
        communicator.setAuthenticated();
        onClientAuthenticated(context, communicator);
    }

    protected void reinitialize(Communicator communicator) {
        communicators.remove(communicator);
        InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
        try {
            initializeClient(initializer);
        } finally {
            pendingCommunicators.add(new PendingCommunicator(
                    communicator,
                    new CommunicatorInitializationContext(),
                    initializer));
        }
    }

    private void initialized(CommunicatorInitializationContext context, Communicator communicator) {
        removePending(communicator);
        communicators.add(communicator);
        communicator.setAuthenticated();
        onClientInitialized(context, communicator);
    }

    private void onDisconnectedEvent(CommunicatorDisconnectedEvent event) {
        Communicator communicator = event.getCommunicator();
        invokeLater(() -> onDisconnected(communicator));
    }

    private PendingCommunicator removePending(Communicator communicator) {
        for (int i = 0; i < pendingCommunicators.size(); ++i) {
            PendingCommunicator pending = pendingCommunicators.get(i);
            if (pending.communicator == communicator) {
                return pendingCommunicators.remove(i);
            }
        }

        return null;
    }

    private void onDisconnected(Communicator communicator) {
        if (communicators.remove(communicator)) {
            // Read final messages
            readAndHandleMessages(communicator);
            onDisconnected(communicator, false);
        } else {
            PendingCommunicator pending = removePending(communicator);
            if (pending != null) {
                onDisconnected(communicator, true);
            } else {
                throw new IllegalStateException(String.format(
                        "Faulty state! Disconnected communicator '%s' is neither active nor pending. Endpoint: %s.",
                        communicator.getId(),
                        communicator.getEndpoint()));
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        readIncoming();
        initializePending();
        onUpdate(deltaTime);
        delayedMonitor.run();
        sendOutGoing();
    }

    /**
     * This method is triggered after reading incoming data, but before sending outgoing.
     *
     * @param deltaTime Time since the last update, in seconds.
     */
    protected abstract void onUpdate(float deltaTime);

    protected synchronized void invokeLater(Runnable runnable) {
        delayedMonitor.delay(runnable);
    }

    private void initializePending() {
        // Iterating backwards allows deletion of the current
        // element when connection has been established.
        for (int i = pendingCommunicators.size() - 1; i >= 0; --i) {
            PendingCommunicator pending = pendingCommunicators.get(i);
            try {
                runInitializationTasks(pending.communicator, pending.context, pending.initializer);
            } catch (IOException e) {
                pending.communicator.disconnect(e);
            }
        }
    }

    private void runInitializationTasks(Communicator communicator, CommunicatorInitializationContext context, InternalCommunicatorInitializer initializer)
            throws IOException {
        InternalInitializationTaskResult result;
        do {
            result = initializer.runTask(context, communicator, this::runInitializationTask);
            if (result == InternalInitializationTaskResult.AWAITING_DATA && communicator.readIncoming(incomingBuffer)) {
                result = initializer.runTask(context, communicator, this::runInitializationTask);
            }
        } while (result == InternalInitializationTaskResult.PENDING);

        if (result == InternalInitializationTaskResult.FINISHED) {
            initialized(context, communicator);
            handleMessages(communicator);
        }
    }

    private boolean runInitializationTask(CommunicatorInitializationContext context, Communicator communicator,
                                      ConditionalInitializationTask conditionalTask) throws IOException {
        if (!conditionalTask.condition.evaluate(context, communicator)) {
            return true;
        }

        InitializationTask task = conditionalTask.task;
        if (task instanceof ConsumerTask) {
            while (true) {
                int remaining = incomingBuffer.remaining();
                if (remaining <= 0) {
                    return false;
                }

                boolean hasCompleted = ((ConsumerTask) task).run(context, communicator, incomingBuffer);
                if (hasCompleted) {
                    return true;
                }

                if (remaining == incomingBuffer.remaining()) {
                    throw new IOException("Unexpected message");
                }
            }
        } else if (task instanceof ProducerTask) {
            ((ProducerTask) task).run(context, communicator);
            return true;
        } else {
            throw new IOException("Unknown  task");
        }
    }

    private void readIncoming() {
        for (int i = 0; i < communicators.size(); ++i) {
            Communicator communicator = communicators.get(i);
            readAndHandleMessages(communicator);
        }
    }

    private void readAndHandleMessages(Communicator communicator) {
        if (communicator.readIncoming(incomingBuffer)) {
            handleMessages(communicator);
        }
    }

    private void handleMessages(Communicator communicator) {
        try {
            while (incomingBuffer.remaining() > 0) {
                onMessage(communicator, incomingBuffer);
            }
        } catch (Exception e) {
            communicator.disconnect(e);
        }
    }

    private void sendOutGoing() {
        sendIndividualMessages();
        sendOutgoingBufferToAll();
    }

    private void sendIndividualMessages() {
        for (int i = 0; i < pendingCommunicators.size(); ++i)
            sendMessages(pendingCommunicators.get(i).communicator);

        for (int i = 0; i < communicators.size(); ++i)
            sendMessages(communicators.get(i));
    }

    private void sendMessages(Communicator communicator) {
        try {
            communicator.sendOutgoing();
        } catch (IOException e) {
            communicator.disconnect(e);
        }
    }

    private void sendOutgoingBufferToAll() {
        if (outgoingBuffer.position() > 0) {
            outgoingBuffer.flip();
            sendToAll(communicators, outgoingBuffer);
            outgoingBuffer.clear();
        }
    }

    protected void sendToAll(Iterable<Communicator> communicators, DataBuffer outgoingBuffer) {
        for (Communicator communicator : communicators) {
            try {
                outgoingBuffer.position(0);
                communicator.sendUpdate(outgoingBuffer);
            } catch (IOException e) {
                communicator.disconnect(e);
            }
        }
    }

    @Override
    public void sendToAll(int message, boolean stream) {
        if (stream) {
            outgoingBuffer.putInt(message);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message);
            }
        }
    }

    @Override
    public void sendToAll(float message, boolean stream) {
        if (stream) {
            outgoingBuffer.putFloat(message);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message);
            }
        }
    }

    @Override
    public void sendToAll(double message, boolean stream) {
        if (stream) {
            outgoingBuffer.putDouble(message);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message);
            }
        }
    }

    @Override
    public void sendToAll(byte message, boolean stream) {
        if (stream) {
            outgoingBuffer.put(message);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message);
            }
        }
    }

    @Override
    public void sendToAll(byte[] message, int off, int len, boolean stream) {
        if (stream) {
            outgoingBuffer.put(message, off, len);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message, off, len);
            }
        }
    }

    @Override
    public void sendToAll(Serializable message, boolean stream) {
        if (stream) {
            message.serialize(outgoingBuffer);
        } else {
            for (int i = 0; i < communicators.size(); ++i) {
                send(communicators.get(i), message);
            }
        }
    }

    @Override
    public void send(Communicator communicator, int message) {
        communicator.getOutgoing().putInt(message);
    }

    @Override
    public void send(Communicator communicator, float message) {
        communicator.getOutgoing().putFloat(message);
    }

    @Override
    public void send(Communicator communicator, double message) {
        communicator.getOutgoing().putDouble(message);
    }

    @Override
    public void send(Communicator communicator, byte message) {
        communicator.getOutgoing().put(message);
    }

    @Override
    public void send(Communicator communicator, byte[] message, int off, int len) {
        communicator.getOutgoing().put(message, off, len);
    }

    @Override
    public void send(Communicator communicator, Serializable message) {
        message.serialize(communicator.getOutgoing());
    }

    @Override
    public final void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            onStart();
        }
    }

    @Override
    public final void stop() throws IOException, InterruptedException {
        if (running.compareAndSet(true, false)) {
            onStop();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    protected abstract void onStart() throws IOException;

    protected abstract void onStop() throws IOException, InterruptedException;

    protected abstract void onConnected(Communicator communicator);

    protected abstract void initializeClient(CommunicatorInitializer initializer);

    protected abstract void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator);

    protected abstract void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator);

    protected abstract void onDisconnected(Communicator communicator, boolean pending);

    protected abstract void onMessage(Communicator communicator, DataBuffer buffer);

    private static class DelayedMonitor {
        private final Deque<Runnable> delayed = new ArrayDeque<>();

        public synchronized void run() {
            while (!delayed.isEmpty()) {
                delayed.poll().run();
            }
        }

        synchronized void delay(Runnable runnable) {
            delayed.addLast(runnable);
        }
    }

    private static class PendingCommunicator {
        final Communicator communicator;
        final CommunicatorInitializationContext context;
        final InternalCommunicatorInitializer initializer;

        PendingCommunicator(Communicator communicator, CommunicatorInitializationContext context, InternalCommunicatorInitializer initializer) {
            this.communicator = communicator;
            this.context = context;
            this.initializer = initializer;
        }
    }
}