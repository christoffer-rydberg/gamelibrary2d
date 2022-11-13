package com.gamelibrary2d.network.client;

import com.gamelibrary2d.network.common.Communicator;
import java.util.concurrent.Future;

public interface Connectable {
    Future<Communicator> connect();
}
