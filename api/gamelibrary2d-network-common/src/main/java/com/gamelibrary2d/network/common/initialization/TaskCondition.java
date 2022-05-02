package com.gamelibrary2d.network.common.initialization;

import com.gamelibrary2d.network.common.Communicator;

public interface TaskCondition {

    TaskCondition TRUE = (cc, c) -> true;

    boolean evaluate(CommunicatorInitializationContext context, Communicator communicator);
}
