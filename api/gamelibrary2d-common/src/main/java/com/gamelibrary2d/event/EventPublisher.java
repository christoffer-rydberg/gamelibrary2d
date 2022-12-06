package com.gamelibrary2d.event;

public interface EventPublisher<T> {

    void publish(T event);

    void addListener(EventListener<T> listener);

    void removeListener(EventListener<T> listener);

}