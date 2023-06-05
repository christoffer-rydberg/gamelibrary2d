package com.gamelibrary2d.components.frames;

public interface PipelineErrorHandler {
    void onError(PipelineContext ctx, Throwable error);
}
