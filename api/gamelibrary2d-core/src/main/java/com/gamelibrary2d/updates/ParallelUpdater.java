package com.gamelibrary2d.updates;

import com.gamelibrary2d.functional.Action;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The ParallelUpdater is an {@link Updater} running all updates in parallel.
 * The updater is finished once all updates have finished.
 */
public class ParallelUpdater implements Updater {
    private Deque<Object> active;
    private Deque<Object> finished;

    public ParallelUpdater() {
        active = new ArrayDeque<>();
        finished = new ArrayDeque<>();
    }

    public ParallelUpdater(int size) {
        active = new ArrayDeque<>(size);
        finished = new ArrayDeque<>(size);
    }

    @Override
    public boolean isFinished() {
        return active.isEmpty();
    }

    @Override
    public void reset() {
        while (!active.isEmpty()) {
            finished.addLast(active.pollFirst());
        }

        finished.forEach(x -> {
            if (x instanceof Update) {
                ((Update) x).reset();
            }
        });

        Deque<Object> tmp = finished;
        finished = active;
        active = tmp;
    }

    @Override
    public void add(Update update) {
        active.addLast(update);
    }

    @Override
    public void add(Action update) {
        active.addLast(update);
    }

    @Override
    public void clear() {
        active.clear();
        finished.clear();
    }

    @Override
    public float update(float deltaTime) {
        float usedTime = 0;
        int count = active.size();
        for (int i = 0; i < count; ++i) {
            usedTime = Math.max(runUpdate(deltaTime), usedTime);
        }
        return usedTime;
    }

    protected float runUpdate(float deltaTime) {
        Object obj = active.pollFirst();

        if (obj instanceof Update) {
            Update update = (Update) obj;
            float usedTime = update.isFinished() ? 0f : update.update(deltaTime);
            if (update.isFinished()) {
                finished.addLast(obj);
            } else {
                active.addLast(update);
            }
            return Math.min(usedTime, deltaTime);
        } else if (obj instanceof Action) {
            ((Action) obj).perform();
            finished.addLast(obj);
            return 0f;
        } else {
            throw new IllegalStateException("Invalid update class");
        }
    }
}