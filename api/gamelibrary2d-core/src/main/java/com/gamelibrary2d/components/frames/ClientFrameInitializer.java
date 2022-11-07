package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.functional.Factory;
import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.client.Client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ClientFrameInitializer {
    private final FrameInitializer initializer;
    private final Client client;
    private final Factory<Future<Communicator>> communicatorFactory;
    private final ParameterizedAction<Communicator> onInitialized;
    private boolean connectionTasksAdded;

    ClientFrameInitializer(
            FrameInitializer initializer,
            Client client,
            Factory<Future<Communicator>> communicatorFactory,
            ParameterizedAction<Communicator> onInitialized) {
        this.initializer = initializer;
        this.client = client;
        this.communicatorFactory = communicatorFactory;
        this.onInitialized = onInitialized;
    }

    /**
     * Adds an initialization task to the pipeline.
     * <p>
     * The task will block the update cycle when it runs.
     *
     * @param task The initialization task
     */
    public void addTask(FrameInitializationTask task) {
        initializer.addTask(task);
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
        initializer.addBackgroundTask(task);
    }

    public void addClientInitialization(ClientFrameInitializer initializer) {
        if (connectionTasksAdded) {
            return;
        }

        connectionTasksAdded = true;

        initializer.addBackgroundTask(ctx -> {
            Communicator communicator = communicatorFactory.create().get();
            client.setCommunicator(communicator);
        });

        initializer.addTask(ctx -> {
            onInitialized.perform(client.getCommunicator());
        });
    }
}
