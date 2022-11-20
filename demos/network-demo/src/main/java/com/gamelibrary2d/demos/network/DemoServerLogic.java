package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.BroadcastService;
import com.gamelibrary2d.network.common.server.ServerLogic;
import java.nio.charset.StandardCharsets;

public class DemoServerLogic implements ServerLogic {

    @Override
    public void onStarted(BroadcastService broadcastService) {

    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onAuthenticateClient(CommunicatorInitializer initializer) {

    }

    @Override
    public void onInitializeClient(CommunicatorInitializer initializer) {
        initializer.addConsumer((ctx, com, inbox) -> {
            readAndPrintMessage(inbox);
            return true;
        });
        initializer.addProducer((ctx, com) -> sendMessage(com, "What?"));
        initializer.addConsumer((ctx, com, inbox) -> {
            readAndPrintMessage(inbox);
            return true;
        });
        initializer.addProducer((ctx, com) -> sendMessage(com, "... Bye!"));
        initializer.addProducer((ctx, com) -> com.disconnect());
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
        System.out.printf("Client: %s%n", message);
    }

    @Override
    public void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        log(String.format("Client has been authenticated: %s", communicator.getEndpoint()));
    }

    @Override
    public void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {

    }

    @Override
    public boolean acceptConnection(String endpoint) {
        log(String.format("Accepting incoming connection: %s", endpoint));
        return true;
    }

    @Override
    public void onConnectionFailed(String endpoint, Exception e) {
        log(String.format("Incoming connection failed: %s", endpoint), e);
    }

    @Override
    public void onConnected(Communicator communicator) {
        log(String.format("Connection established: %s", communicator.getEndpoint()));
    }

    @Override
    public void onDisconnected(Communicator communicator, boolean pending, Throwable cause) {
        log(String.format("Client disconnected: %s", communicator.getEndpoint()));
    }

    @Override
    public void onUpdate(float deltaTime) {

    }

    @Override
    public void onMessage(Communicator communicator, DataBuffer buffer) {

    }

    private void log(String message) {
        System.out.println(message);
    }

    private void log(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }
}