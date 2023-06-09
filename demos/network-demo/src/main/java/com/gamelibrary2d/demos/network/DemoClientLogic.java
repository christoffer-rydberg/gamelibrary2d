package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.ClientLogic;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.nio.charset.StandardCharsets;

public class DemoClientLogic implements ClientLogic {
    @Override
    public void onInitialize(ConnectionInitializer initializer) {
        initializer.addProducer((ctx, com) -> sendMessage(com, "What do you call a guy with a rubber toe?"));
        initializer.addConsumer((ctx, com, inbox) -> {
            readAndPrintMessage(inbox);
            return true;
        });
        initializer.addProducer((ctx, com) -> sendMessage(com, "Roberto."));
        initializer.addConsumer((ctx, com, inbox) -> {
            readAndPrintMessage(inbox);
            return true;
        });
    }

    private void sendMessage(Communicator communicator, String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        communicator.getOutgoing().putInt(bytes.length);
        communicator.getOutgoing().put(bytes);
    }

    private void readAndPrintMessage(DataBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        String message = new String(bytes, StandardCharsets.UTF_8);
        System.out.printf("Server: %s%n", message);
    }

    @Override
    public void onMessage(DataBuffer buffer) {

    }
}