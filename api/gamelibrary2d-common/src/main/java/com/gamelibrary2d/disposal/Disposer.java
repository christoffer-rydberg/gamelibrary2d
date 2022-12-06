package com.gamelibrary2d.disposal;

public interface Disposer extends Disposable {
    void registerDisposal(Disposable disposable);
}