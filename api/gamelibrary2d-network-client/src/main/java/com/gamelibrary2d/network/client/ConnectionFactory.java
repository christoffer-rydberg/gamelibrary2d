package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.Communicator;

import java.util.concurrent.Future;

public interface ConnectionFactory {
    Future<Communicator> createConnection();
}
