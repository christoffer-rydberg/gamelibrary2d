package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.functional.Factory;
import com.gamelibrary2d.functional.ParameterizedAction;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public class ClientFrameInitializer {
    private final Function<ParameterizedAction<CommunicatorInitializer>, Client> clientFactory;
    private final AbstractClientFrame frame;
    private final FrameInitializer frameInitializer;
    private boolean clientTasksAdded;

    ClientFrameInitializer(
            Function<ParameterizedAction<CommunicatorInitializer>, Client> clientFactory,
            AbstractClientFrame frame,
            FrameInitializer frameInitializer) {
        this.clientFactory = clientFactory;
        this.frame = frame;
        this.frameInitializer = frameInitializer;
    }

    /**
     * Adds an initialization task to the pipeline.
     * <p>
     * The task will block the update cycle when it runs.
     *
     * @param task The initialization task
     */
    public void addTask(FrameInitializationTask task) {
        frameInitializer.addTask(task);
    }

    /**
     * Adds an initialization task to the pipeline that will run in the background.
     * <br><br>The task will not block the update cycle when it runs, but since it's not running on the main thread
     * it won't have access to the OpenGL context.
     * <br><br>Background tasks are generally safe to have side effects.
     * The underlying {@link CompletableFuture} assures that the main thread will have visibility of changed fields when
     * the task has completed. Fields that are accessed or modified in parallel with the task must be properly synchronized.
     *
     * @param task The initialization task
     */
    public void addBackgroundTask(FrameInitializationTask task) {
        frameInitializer.addBackgroundTask(task);
    }

    /**
     * Adds tasks to connect and initialize the client.
     */
    public void initializeClient(
            Factory<Future<Communicator>> connectionFactory,
            ParameterizedAction<CommunicatorInitializer> onInitialize,
            ParameterizedAction<Communicator> onInitialized) {
        if (clientTasksAdded) {
            return;
        }

        clientTasksAdded = true;

        final Client client = clientFactory.apply(onInitialize);

        addBackgroundTask(ctx -> {
            Communicator communicator = connectionFactory.create().get();
            client.initialize(communicator);
        });

        addTask(ctx -> {
            frame.onClientInitialized(client);
            onInitialized.perform(client.getCommunicator());
        });
    }
}
