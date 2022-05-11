package com.gamelibrary2d.network.common.client;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;
import com.gamelibrary2d.network.common.exceptions.ConnectionException;
import com.gamelibrary2d.network.common.initialization.*;
import com.gamelibrary2d.network.common.initialization.ConditionalInitializationTask;
import com.gamelibrary2d.network.common.initialization.InitializationTask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractClient implements Client {
    private final DataBuffer inbox;
    private Communicator communicator;
    private int initializationRetries = 100;
    private int initializationRetryDelay = 100;

    protected AbstractClient() {
        this.inbox = new DynamicByteBuffer();
        inbox.flip();
    }

    private static void sendOutgoing(Communicator com) throws IOException {
        try {
            com.sendOutgoing();
        } catch (IOException e) {
            com.disconnect(e);
            throw e;
        }
    }

    private void clearInbox() {
        inbox.clear();
        inbox.flip();
    }

    @Override
    public boolean isConnected() {
        return communicator != null && communicator.isConnected();
    }

    @Override
    public void disconnect() {
        if (communicator != null) {
            communicator.disconnect();
        }
    }

    @Override
    public void connect() throws ConnectionException, ClientAuthenticationException, ClientInitializationException {
        try {
            this.communicator = connectCommunicator().get();

            if (!communicator.isConnected()) {
                throw new ConnectionException("The communicator is not connected");
            }

            clearInbox();
            communicator.clearOutgoing();
            CommunicatorInitializationContext context = new CommunicatorInitializationContext();
            authenticate(context, communicator);
            initialize(context, communicator);
        } catch (InterruptedException | ExecutionException e) {
            communicator.disconnect(e);
            this.communicator = null;
            throw new ConnectionException("Failed to connect communicator", e);
        } catch (Exception e) {
            communicator.disconnect(e);
            this.communicator = null;
            throw e;
        }
    }

    private void authenticate(CommunicatorInitializationContext context, Communicator communicator) throws ClientAuthenticationException {
        if (!communicator.isAuthenticated()) {
            InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
            authenticateConnection(communicator, initializer);
            try {
                runInitializationTasks(context, communicator, new InternalInitializationTaskManager(initializer.getAll()));
            } catch (IOException | InterruptedException e) {
                throw new ClientAuthenticationException("Authentication failed", e);
            }

            communicator.setAuthenticated();
        }
    }

    private void initialize(CommunicatorInitializationContext context, Communicator communicator)
            throws ClientInitializationException {
        InternalCommunicatorInitializer initializer = new InternalCommunicatorInitializer();
        initialize(initializer);
        try {
            runInitializationTasks(context, communicator, new InternalInitializationTaskManager(initializer.getAll()));
        } catch (IOException | InterruptedException e) {
            throw new ClientInitializationException("Initialization failed", e);
        }
    }

    /**
     * The max number of retries for each task.
     */
    protected int getInitializationRetries() {
        return initializationRetries;
    }

    /**
     * Sets the number of {@link #getInitializationRetries() initialization retries}.
     */
    protected void setInitializationRetries(int initializationRetries) {
        this.initializationRetries = initializationRetries;
    }

    /**
     * The delay between retries of tasks in milliseconds.
     */
    protected int getInitializationRetryDelay() {
        return initializationRetryDelay;
    }

    /**
     * Sets the {@link #getInitializationRetries() initialization retry delay}.
     */
    protected void setInitializationRetryDelay(int initializationRetryDelay) {
        this.initializationRetryDelay = initializationRetryDelay;
    }

    @Override
    public Communicator getCommunicator() {
        return communicator;
    }

    @Override
    public void readIncoming() {
        handleMessages();
        Communicator com = getCommunicator();
        if (com != null && refreshInboxIfEmpty(com)) {
            handleMessages();
        }
    }

    @Override
    public void sendOutgoing() {
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

    private void runInitializationTasks(CommunicatorInitializationContext context, Communicator communicator, InternalInitializationTaskManager connector)
            throws IOException, InterruptedException {
        InternalInitializationTaskManager.InitializationTaskResult result;

        int retries = 0;
        do {
            result = connector.runTask(context, communicator, this::runInitializationTask);
            if (result == InternalInitializationTaskManager.InitializationTaskResult.AWAITING_DATA) {
                if (retries == getInitializationRetries()) {
                    throw new IOException("Reading server response timed out");
                }

                if (!communicator.isConnected()) {
                    throw new IOException("Connection has been lost");
                }

                ++retries;

                sendOutgoing(communicator);

                Thread.sleep(getInitializationRetryDelay());
            } else {
                retries = 0;
            }

        } while (result != InternalInitializationTaskManager.InitializationTaskResult.FINISHED);

        sendOutgoing(communicator);
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

    private void authenticateConnection(Communicator communicator, CommunicatorInitializer initializer) {
        initializer.addConsumer(this::readCommunicatorId);
        communicator.configureAuthentication(initializer);
    }

    private boolean readCommunicatorId(CommunicatorInitializationContext context, Communicator communicator, DataBuffer inbox) {
        int communicatorId = inbox.getInt();
        communicator.setId(communicatorId);
        return true;
    }

    protected abstract Future<Communicator> connectCommunicator();

    protected abstract void initialize(CommunicatorInitializer initializer);

    protected abstract void onMessage(DataBuffer buffer);
}