package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.common.Communicator;
import java.util.concurrent.CompletableFuture;

public class ClientFrameInitializer {
    private final Client client;
    private final AbstractClientFrame frame;
    private final FrameInitializer frameInitializer;
    private boolean connectionTasksAdded;

    ClientFrameInitializer(
            Client client,
            AbstractClientFrame frame,
            FrameInitializer frameInitializer) {
        this.client = client;
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
    public void addClientTasks() {
        if (connectionTasksAdded) {
            return;
        }

        connectionTasksAdded = true;

        addBackgroundTask(ctx -> {
            Communicator communicator = frame.connectToServer().get();
            client.initialize(communicator);
        });

        addTask(ctx -> {
            frame.onClientInitialized(client.getCommunicator());
        });
    }
}
