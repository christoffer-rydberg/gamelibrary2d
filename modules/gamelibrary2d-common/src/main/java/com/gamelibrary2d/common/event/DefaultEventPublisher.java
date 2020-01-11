package com.gamelibrary2d.common.event;

import java.util.ArrayList;

public class DefaultEventPublisher<T> implements EventPublisher<T> {

    private final int defaultSize;

    private ArrayList<EventListener<T>> listeners;

    private ArrayList<EventListener<T>> iterationList;

    public DefaultEventPublisher() {
        defaultSize = -1;
    }

    public DefaultEventPublisher(int size) {
        defaultSize = size;
    }

    @Override
    public boolean publish(T event) {
        int size = listeners != null ? listeners.size() : 0;
        if (size == 0)
            return false;
        populateIterationListIfEmpty();
        for (int i = 0; i < size; ++i)
            iterationList.get(i).onEvent(event);
        return true;
    }

    @Override
    public void addListener(EventListener<T> listener) {
        allocateListenersIfNull();
        listeners.add(listener);
        clearIterationList();
    }

    @Override
    public void removeListener(EventListener<T> listener) {
        allocateListenersIfNull();
        listeners.remove(listener);
        clearIterationList();
    }

    private void allocateListenersIfNull() {
        if (listeners == null) {
            listeners = defaultSize > 0 ? new ArrayList<>(defaultSize)
                    : new ArrayList<>();
        }
    }

    private void populateIterationListIfEmpty() {
        if (iterationList == null) {
            iterationList = new ArrayList<>(listeners);
        } else if (iterationList.isEmpty()) {
            iterationList.addAll(listeners);
        }
    }

    private void clearIterationList() {
        if (iterationList != null) {
            iterationList.clear();
        }
    }
}