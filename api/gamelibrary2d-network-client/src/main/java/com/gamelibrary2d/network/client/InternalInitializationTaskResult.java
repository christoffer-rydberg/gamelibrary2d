package com.gamelibrary2d.network.client;

enum InternalInitializationTaskResult {
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
