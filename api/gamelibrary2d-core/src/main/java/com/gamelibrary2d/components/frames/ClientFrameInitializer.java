package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ClientFrameInitializer {
    private final FrameInitializer frameInitializer;
    private final Client client;
    private boolean connectionTasksAdded;

    ClientFrameInitializer(FrameInitializer frameInitializer, Client client) {
        this.frameInitializer = frameInitializer;
        this.client = client;
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

    public void addClientInitialization(ClientFrameInitializer initializer) {
        if (connectionTasksAdded) {
            return;
        }

        connectionTasksAdded = true;

        initializer.addBackgroundTask(ctx -> {
            Communicator communicator = client.connect().get();
            client.initialize(communicator);
        });

        initializer.addTask(ctx -> {
            client.onInitialized();
        });
    }

    interface Client {
        Future<Communicator> connect();
        void initialize(Communicator communicator) throws ClientAuthenticationException, ClientInitializationException;
        void onInitialized();
    }
}
