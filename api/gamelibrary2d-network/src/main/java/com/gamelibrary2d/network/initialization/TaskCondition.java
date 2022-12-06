package com.gamelibrary2d.network.initialization;

import com.gamelibrary2d.network.Communicator;

public interface TaskCondition {

    TaskCondition TRUE = (cc, c) -> true;

    boolean evaluate(CommunicatorInitializationContext context, Communicator communicator);
}
