package com.gamelibrary2d.network.server;

import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.functional.Factory;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.events.CommunicatorDisconnectedEvent;
import com.gamelibrary2d.network.events.CommunicatorDisconnectedListener;
import com.gamelibrary2d.network.initialization.*;
import com.gamelibrary2d.random.RandomInstance;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServer implements Server, Updatable {
    private final Factory<Integer> communicatorIdFactory = RandomInstance.get()::nextInt;
    private final List<PendingCommunicator> pendingCommunicators;
    private final DataBuffer streamBuffer;
    private final DataBuffer incomingBuffer;
    private final ArrayList<Communicator> communicators;
    private final List<Communicator> readOnlyCommunicators;
    private final DelayedMonitor delayedMonitor = new DelayedMonitor();
    private final CommunicatorDisconnectedListener disconnectedEventListener = this::onDisconnectedEvent;
    private final AtomicBoolean running = new AtomicBoolean();

    protected AbstractServer() {
        incomingBuffer = new DynamicByteBuffer();
        incomingBuffer.flip();
        streamBuffer = new DynamicByteBuffer();
        communicators = new ArrayList<>();
        pendingCommunicators = new ArrayList<>();
        readOnlyCommunicators = Collections.unmodifiableList(communicators);
    }

    protected void addPendingCommunicator(Communicator communicator) {
        InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
        initializer.addProducer((__, com) -> writeIdentifier(com, communicatorIdFactory));
        initializer.addProducer(this::connectedTask);
        communicator.configureAuthentication(initializer);
        initializer.addProducer(this::authenticatedTask);
        onInitializeClient(initializer);

        pendingCommunicators.add(new PendingCommunicator(
                communicator,
                new CommunicatorInitializationContext(),
                initializer));

        communicator.addDisconnectedListener(disconnectedEventListener);
    }

    protected List<Communicator> getCommunicators() {
        return readOnlyCommunicators;
    }

    protected DataBuffer getStreamBuffer() {
        return streamBuffer;
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
        if (communicators.remove(communicator)) {
            InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
            try {
                onInitializeClient(initializer);
            } finally {
                pendingCommunicators.add(new PendingCommunicator(
                        communicator,
                        new CommunicatorInitializationContext(),
                        initializer));
            }
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
        invokeLater(() -> onDisconnected(communicator, event.getCause()));
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

    private void onDisconnected(Communicator communicator, Throwable cause) {
        if (communicators.remove(communicator)) {
            // Read final messages
            readAndHandleMessages(communicator);
            onDisconnected(communicator, false, cause);
        } else {
            PendingCommunicator pending = removePending(communicator);
            if (pending != null) {
                onDisconnected(communicator, true, cause);
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
                runInitializationTasks(pending);
            } catch (IOException e) {
                pending.communicator.disconnect(e);
            }
        }
    }

    private void runInitializationTasks(PendingCommunicator pending)
            throws IOException {
        InternalInitializationTaskResult result;

        do {
            result = pending.initializer.runTask(pending.context, pending.communicator, this::runInitializationTask);
            if (result == InternalInitializationTaskResult.AWAITING_DATA && pending.communicator.readIncoming(incomingBuffer)) {
                result = pending.initializer.runTask(pending.context, pending.communicator, this::runInitializationTask);
            }

            if (result == InternalInitializationTaskResult.AWAITING_DATA) {
                long currentTime = System.currentTimeMillis();

                ++pending.retryAttempt;
                if (pending.retryAttempt == 1) {
                    pending.retryStart = currentTime;
                }

                long timeSinceRetriesStarted = currentTime - pending.retryStart;

                float maxRetryTime = pending.initializer.getRetries() * pending.initializer.getRetryDelay();

                if (timeSinceRetriesStarted > maxRetryTime) {
                    pending.communicator.disconnect(new IOException("Initialization retry time exceeded"));
                    return;
                }
            } else {
                pending.retryAttempt = 0;
            }
        } while (result == InternalInitializationTaskResult.PENDING);

        if (result == InternalInitializationTaskResult.FINISHED) {
            initialized(pending.context, pending.communicator);
            handleMessages(pending.communicator);
        }
    }

    private boolean runInitializationTask(
            CommunicatorInitializationContext context,
            Communicator communicator,
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
                    communicator.sendOutgoing();
                    return true;
                }

                if (remaining == incomingBuffer.remaining()) {
                    throw new IOException("Unexpected message");
                }
            }
        } else if (task instanceof ProducerTask) {
            ((ProducerTask) task).run(context, communicator);
            communicator.sendOutgoing();
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
        flushStreamBuffer();
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

    private void flushStreamBuffer() {
        if (streamBuffer.position() > 0) {
            streamBuffer.flip();
            stream(streamBuffer);
            streamBuffer.clear();
        }
    }

    private void stream(DataBuffer buffer) {
        for (Communicator communicator : communicators) {
            try {
                buffer.position(0);
                communicator.stream(buffer);
            } catch (IOException e) {
                communicator.disconnect(e);
            }
        }
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
    public final boolean isRunning() {
        return running.get();
    }

    protected abstract void onStart() throws IOException;

    protected abstract void onStop() throws IOException, InterruptedException;

    protected abstract void onConnected(Communicator communicator);

    protected abstract void onInitializeClient(CommunicatorInitializer initializer);

    protected abstract void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator);

    protected abstract void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator);

    protected abstract void onDisconnected(Communicator communicator, boolean pending, Throwable cause);

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
        public long retryStart;
        public int retryAttempt;

        PendingCommunicator(Communicator communicator, CommunicatorInitializationContext context, InternalCommunicatorInitializer initializer) {
            this.communicator = communicator;
            this.context = context;
            this.initializer = initializer;
        }
    }
}