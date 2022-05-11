package com.gamelibrary2d.network.common.server;

import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.ConditionalInitializationTask;
import com.gamelibrary2d.network.common.initialization.InitializationTaskRunner;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

class InternalInitializationTaskManager {
    private final Deque<ConditionalInitializationTask> tasks;

    public InternalInitializationTaskManager(Collection<ConditionalInitializationTask> tasks) {
        this.tasks = new ArrayDeque<>(tasks);
    }

    public InitializationTaskResult runTask(
            CommunicatorInitializationContext context,
            Communicator communicator,
            InitializationTaskRunner runner)
            throws IOException {

        if (tasks.isEmpty()) {
            return InitializationTaskResult.FINISHED;
        }

        ConditionalInitializationTask next = tasks.peekFirst();
        if (runner.run(context, communicator, next)) {
            tasks.pollFirst();
            return tasks.isEmpty() ? InitializationTaskResult.FINISHED : InitializationTaskResult.PENDING;
        }
        return InitializationTaskResult.AWAITING_DATA;
    }

    public enum InitializationTaskResult {
        /**
         * All tasks have finished.
         */
        FINISHED,

        /**
         * The current task requires more data.
         */
        AWAITING_DATA,

        /**
         * The next task is ready to run.
         */
        PENDING
    }
}