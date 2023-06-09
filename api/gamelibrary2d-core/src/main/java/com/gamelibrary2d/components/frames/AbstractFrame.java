package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.containers.AbstractLayer;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.Client;
import com.gamelibrary2d.network.client.ClientLogic;
import com.gamelibrary2d.network.client.ConnectionFactory;
import com.gamelibrary2d.network.client.DefaultClient;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.updates.Update;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class AbstractFrame extends AbstractLayer<Renderable> implements Frame {
    private final DelayedActionMonitor delayedActionMonitor = new DelayedActionMonitor();
    private final Deque<Update> updates = new ArrayDeque<>();
    private final Deque<PipelineManager> pipelines = new ArrayDeque<>();
    private final DefaultDisposer disposer;
    private Color backgroundColor = Color.BLACK;
    private Client client;
    private int pipelinesLeftToRunInUpdate;

    protected AbstractFrame(Disposer parentDisposer) {
        this.disposer = new DefaultDisposer(parentDisposer);
    }

    @Override
    public void invokeLater(Action action) {
        delayedActionMonitor.add(action);
    }

    @Override
    public void registerDisposal(Disposable disposable) {
        disposer.registerDisposal(disposable);
    }

    @Override
    public void begin() {
        clearDelayedActions();
        onBegin();
    }

    /**
     * Removes all actions that have been queued by {@link #invokeLater}.
     */
    protected void clearDelayedActions() {
        delayedActionMonitor.clear();
    }

    protected void startPipeline(PipelineConfiguration configuration, PipelineErrorHandler errorHandler) {
        PipelineContext context = new PipelineContext();

        Pipeline pipeline = new Pipeline(
                this,
                context);

        configuration.addTasks(pipeline);

        Future<Void> work = pipeline.run();

        PipelineManager pipelineManager = new PipelineManager(work, context, errorHandler);

        if (!tryFinishPipeline(pipelineManager)) {
            pipelines.addLast(pipelineManager);
        }
    }

    private void runPipelines() {
        try {
            pipelinesLeftToRunInUpdate = pipelines.size();

            while (pipelinesLeftToRunInUpdate > 0) {
                PipelineManager pipelineManager = pipelines.pollFirst();
                if (!tryFinishPipeline(pipelineManager)) {
                    pipelines.addLast(pipelineManager);
                }

                --pipelinesLeftToRunInUpdate;
            }
        } finally {
            pipelinesLeftToRunInUpdate = 0;
        }
    }

    private boolean tryFinishPipeline(PipelineManager pipelineManager) {
        if (pipelineManager.work.isDone()) {
            try {
                pipelineManager.work.get();
            } catch (InterruptedException | ExecutionException e) {
                pipelineManager.errorHandler.onError(pipelineManager.context, e);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void end() {
        onEnd();
        clearDelayedActions();
    }

    private void cancelPipelines() {
        pipelinesLeftToRunInUpdate = 0;
        int size = pipelines.size();
        for (int i = 0; i < size; ++i) {
            PipelineManager pipelineManager = pipelines.pollFirst();
            pipelineManager.work.cancel(true);
        }
    }

    @Override
    public void dispose() {
        cancelPipelines();
        clear();
        disposer.dispose();
        disposer.clear();
        updates.clear();
        clearDelayedActions();
        onDispose();
        client = null;
    }

    @Override
    public void startUpdate(Update update) {
        if (!updates.contains(update)) {
            updates.addLast(update);
        }
    }

    @Override
    public void stopUpdate(Update update) {
        updates.remove(update);
    }

    @Override
    protected final void handleUpdate(float deltaTime) {
        delayedActionMonitor.run();
        runPipelines();
        onUpdate(deltaTime);
    }

    protected void onUpdate(float deltaTime) {
        if (client != null) {
            client.update(deltaTime);
        } else {
            performUpdate(deltaTime);
        }
    }

    private void performUpdate(float deltaTime) {
        super.handleUpdate(deltaTime);

        for (int i = 0; i < updates.size(); ++i) {
            Update update = updates.pollFirst();
            update.update(deltaTime);
            if (!update.isFinished()) {
                updates.addLast(update);
            }
        }
    }

    @Override
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    protected void setBackgroundColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Background color cannot be null");
        }

        this.backgroundColor = color;
    }

    /**
     * Invoked when the frame begins.
     */
    protected abstract void onBegin();

    /**
     * Invoked when the frame ends.
     */
    protected abstract void onEnd();

    /**
     * Invoked when the frame is disposed.
     */
    protected abstract void onDispose();

    private static class DelayedActionMonitor {
        private final Deque<Action> actions = new ArrayDeque<>();

        private int actionsLeftToRunInUpdate;

        synchronized void add(Action action) {
            actions.add(action);
        }

        synchronized void run() {
            try {
                actionsLeftToRunInUpdate = actions.size();
                while (actionsLeftToRunInUpdate > 0) {
                    actions.pollFirst().perform();
                    --actionsLeftToRunInUpdate;
                }
            } finally {
                actionsLeftToRunInUpdate = 0;
            }
        }

        synchronized void clear() {
            actions.clear();
            actionsLeftToRunInUpdate = 0;
        }
    }

    /**
     * Starts a pipeline to connect the frame to a server.
     */
    protected void connectToServer(FrameClient frameClient, ConnectionFactory connectionFactory, PipelineErrorHandler errorHandler) {
        startPipeline(pipeline -> connectToServer(frameClient, connectionFactory, pipeline), errorHandler);
    }

    /**
     * Adds tasks to the specified pipeline to connect the frame to a server.
     */
    protected void connectToServer(FrameClient frameClient, ConnectionFactory connectionFactory, Pipeline pipeline) {
        final Client client = new DefaultClient(new FrameClientLogic(frameClient), this::performUpdate);

        pipeline.addBackgroundTask(ctx -> {
            Communicator communicator = connectionFactory.createConnection().get();
            client.initialize(communicator);
        });

        pipeline.addTask(ctx -> {
            this.client = client;
            frameClient.onClientInitialized(client.getCommunicator());
        });
    }

    private static class FrameClientLogic implements ClientLogic {
        private final FrameClient frameClient;

        private FrameClientLogic(FrameClient frameClient) {
            this.frameClient = frameClient;
        }

        @Override
        public void onInitialize(ConnectionInitializer initializer) {
            frameClient.onInitializeClient(initializer);
        }

        @Override
        public void onMessage(DataBuffer buffer) {
            frameClient.onMessage(buffer);
        }
    }

    private static class PipelineManager {
        private final Future<Void> work;
        private final PipelineContext context;
        private final PipelineErrorHandler errorHandler;
        public PipelineManager(Future<Void> work, PipelineContext context, PipelineErrorHandler errorHandler) {
            this.work = work;
            this.context = context;
            this.errorHandler = errorHandler;
        }
    }
}