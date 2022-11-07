package com.gamelibrary2d.network.common.client;

import com.gamelibrary2d.network.common.Communicator;

import java.util.concurrent.Future;

public interface CommunicatorConnector {
    Future<Communicator> connect();
}
