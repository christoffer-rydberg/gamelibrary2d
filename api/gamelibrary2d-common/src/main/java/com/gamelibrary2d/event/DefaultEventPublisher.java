package com.gamelibrary2d.event;

public class DefaultEventPublisher<T> extends AbstractEventPublisher<T> {

    public DefaultEventPublisher() {

    }

    public DefaultEventPublisher(int size) {
        super(size);
    }
}