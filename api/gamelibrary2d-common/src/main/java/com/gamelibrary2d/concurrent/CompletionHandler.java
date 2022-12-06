package com.gamelibrary2d.concurrent;

import java.util.concurrent.ExecutionException;

public interface CompletionHandler<T1, T2> {
    T2 handle(T1 t) throws ExecutionException;
}