package com.gamelibrary2d.updates;

import com.gamelibrary2d.common.functional.Action;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The SequentialUpdater is an {@link Updater} running all updates
 * sequentially, in the order they were added. The updater is finished when all
 * updates have finished.
 */
public class SequentialUpdater implements Updater {
    private Deque<Object> active;
    private Deque<Object> finished;

    public SequentialUpdater() {
        active = new ArrayDeque<>();
        finished = new ArrayDeque<>();
    }

    public SequentialUpdater(int size) {
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
        float remainingTime = deltaTime;
        while (!active.isEmpty() && remainingTime > 0) {
            float usedTime = runUpdate(remainingTime);
            remainingTime -= usedTime;
        }

        return deltaTime - remainingTime;
    }

    protected float runUpdate(float deltaTime) {
        Object obj = active.peekFirst();

        if (obj instanceof Updater) {
            Updater update = (Updater) obj;
            float usedTime = update.isFinished() ? 0f : update.update(deltaTime);
            if (update.isFinished()) {
                finished.addLast(active.pollFirst());
            }
            return Math.min(usedTime, deltaTime);
        } else if (obj instanceof Action) {
            ((Action) obj).perform();
            finished.addLast(active.pollFirst());
            return 0f;
        } else {
            throw new IllegalStateException("Invalid update class");
        }
    }
}