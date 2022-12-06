package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.Communicator;

import java.util.concurrent.Future;

public interface Connectable {
    Future<Communicator> connect();
}
