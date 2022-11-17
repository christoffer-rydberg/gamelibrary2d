package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.server.ServerLogic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DemoServer implements ServerLogic {
    private int messagesReceived;

    @Override
    public void onAuthenticateClient(CommunicatorInitializer initializer) {

    }

    @Override
    public void onInitializeClient(CommunicatorInitializer initializer) {

    }

    @Override
    public void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        log(String.format("Client has been authenticated: %s", communicator.getEndpoint()));
    }

    @Override
    public void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
        log(String.format("Client has been initialized: %s", communicator.getEndpoint()));
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onStopped() {

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
    public void onDisconnected(Communicator communicator, boolean pending) {
        log(String.format("Connection lost: %s", communicator.getEndpoint()));
    }

    @Override
    public void onUpdate(float deltaTime) {

    }

    private void sendMessage(Communicator communicator, String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        communicator.getOutgoing().putInt(bytes.length);
        communicator.getOutgoing().put(bytes);
    }

    @Override
    public void onMessage(Communicator communicator, DataBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        String message = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(String.format("Client: %s", message));

        ++messagesReceived;
        if (messagesReceived == 1) {
            sendMessage(communicator, "What?");
        } else {
            sendMessage(communicator, "... Bye.");
            try {
                communicator.sendOutgoing();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                communicator.disconnect();
            }
        }
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void log(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }
}