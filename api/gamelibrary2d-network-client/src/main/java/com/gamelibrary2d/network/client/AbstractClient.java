package com.gamelibrary2d.network.client;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.exceptions.ClientInitializationException;
import com.gamelibrary2d.network.initialization.*;

import java.io.IOException;

public abstract class AbstractClient implements Client {
    private final DataBuffer inbox;
    private Communicator communicator;

    protected AbstractClient() {
        this.inbox = new DynamicByteBuffer();
        inbox.flip();
    }

    @Override
    public void initialize(Communicator communicator) throws ClientAuthenticationException, ClientInitializationException {
        try {
            this.communicator = communicator;

            clearInbox();
            communicator.clearOutgoing();
            CommunicatorInitializationContext context = new CommunicatorInitializationContext();
            authenticate(context, communicator);
            initialize(context, communicator);
        } catch (Exception e) {
            communicator.disconnect(e);
            this.communicator = null;
            throw e;
        }
    }

    private void clearInbox() {
        inbox.clear();
        inbox.flip();
    }

    private void authenticate(CommunicatorInitializationContext context, Communicator communicator) throws ClientAuthenticationException {
        if (!communicator.isAuthenticated()) {
            InternalConnectionInitializer initializer = new InternalConnectionInitializer();
            authenticateConnection(communicator, initializer);
            try {
                runInitializationTasks(context, communicator, initializer);
            } catch (IOException | InterruptedException e) {
                throw new ClientAuthenticationException("Authentication failed", e);
            }

            communicator.setAuthenticated();
        }
    }

    private void initialize(CommunicatorInitializationContext context, Communicator communicator)
            throws ClientInitializationException {
        InternalConnectionInitializer initializer = new InternalConnectionInitializer();
        onInitialize(initializer);
        try {
            runInitializationTasks(context, communicator, initializer);
        } catch (IOException | InterruptedException e) {
            throw new ClientInitializationException("Initialization failed", e);
        }
    }

    @Override
    public Communicator getCommunicator() {
        return communicator;
    }

    protected void readIncoming() {
        handleMessages();
        Communicator com = getCommunicator();
        if (com != null && refreshInboxIfEmpty(communicator)) {
            handleMessages();
        }
    }

    protected void sendOutgoing() {
        if (isConnected()) {
            Communicator com = getCommunicator();
            try {
                com.sendOutgoing();
            } catch (IOException e) {
                com.disconnect(e);
            }
        }
    }

    private void handleMessages() {
        while (inbox.remaining() > 0) {
            onMessage(inbox);
        }
    }

    private boolean refreshInboxIfEmpty(Communicator communicator) {
        return inbox.remaining() > 0 || communicator.readIncoming(inbox);
    }

    private void runInitializationTasks(CommunicatorInitializationContext context, Communicator communicator, InternalConnectionInitializer initializer)
            throws IOException, InterruptedException {
        InternalInitializationTaskResult result;

        int retries = 0;
        do {
            result = initializer.runTask(context, communicator, this::runInitializationTask);
            if (result == InternalInitializationTaskResult.AWAITING_DATA) {
                if (retries == initializer.getRetries()) {
                    throw new IOException("Reading server response timed out");
                }

                if (!communicator.isConnected()) {
                    throw new IOException("Connection has been lost");
                }

                ++retries;

                sendOutgoing(communicator);

                Thread.sleep(initializer.getRetryDelay());
            } else {
                retries = 0;
            }

        } while (result != InternalInitializationTaskResult.FINISHED);

        sendOutgoing(communicator);
    }

    private static void sendOutgoing(Communicator com) throws IOException {
        try {
            com.sendOutgoing();
        } catch (IOException e) {
            com.disconnect(e);
            throw e;
        }
    }

    private boolean runInitializationTask(CommunicatorInitializationContext context, Communicator communicator,
                                          ConditionalInitializationTask conditionalTask) throws IOException {
        if (!conditionalTask.condition.evaluate(context, communicator)) {
            return true;
        }

        InitializationTask task = conditionalTask.task;
        if (task instanceof ConsumerTask) {
            try {
                return refreshInboxIfEmpty(communicator) && ((ConsumerTask) task).run(context, communicator, inbox);
            } catch (IOException e) {
                communicator.disconnect(e);
                throw (e);
            }
        } else if (task instanceof ProducerTask) {
            ((ProducerTask) task).run(context, communicator);
            return true;
        } else {
            throw new IllegalStateException("Unknown task: " + task.getClass().getName());
        }
    }

    private void authenticateConnection(Communicator communicator, ConnectionInitializer initializer) {
        initializer.addConsumer(this::readCommunicatorId);
        communicator.configureAuthentication(initializer);
    }

    private boolean readCommunicatorId(CommunicatorInitializationContext context, Communicator communicator, DataBuffer inbox) {
        int communicatorId = inbox.getInt();
        communicator.setId(communicatorId);
        return true;
    }

    protected abstract void onInitialize(ConnectionInitializer initializer);

    protected abstract void onMessage(DataBuffer buffer);
}