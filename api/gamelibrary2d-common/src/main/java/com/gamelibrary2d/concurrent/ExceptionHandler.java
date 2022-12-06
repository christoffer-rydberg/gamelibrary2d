package com.gamelibrary2d.concurrent;

public interface ExceptionHandler<T> {
    T handle(Throwable e) throws NotHandledException;
}
