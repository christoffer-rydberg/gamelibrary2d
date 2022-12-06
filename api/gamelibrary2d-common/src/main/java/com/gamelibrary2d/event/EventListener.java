package com.gamelibrary2d.event;

public interface EventListener<T> {
    void onEvent(T event);
}