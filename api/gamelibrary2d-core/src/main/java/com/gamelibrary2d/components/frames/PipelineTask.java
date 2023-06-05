package com.gamelibrary2d.components.frames;

public interface PipelineTask {
    void perform(PipelineContext context) throws Throwable;
}
