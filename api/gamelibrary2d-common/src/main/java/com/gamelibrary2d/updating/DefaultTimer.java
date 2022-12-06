package com.gamelibrary2d.updating;

public class DefaultTimer implements Timer {
    @Override
    public double getTime() {
        return System.nanoTime() / 1000000000.0;
    }
}
