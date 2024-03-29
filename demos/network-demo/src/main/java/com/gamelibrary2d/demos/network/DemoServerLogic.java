package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.network.server.Host;
import com.gamelibrary2d.network.server.ServerLogic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DemoServerLogic implements ServerLogic {
    private final int connectionTcpPort;

    public DemoServerLogic() {
        this(0);
    }

    public DemoServerLogic(int connectionTcpPort) {
        this.connectionTcpPort = connectionTcpPort;
    }

    @Override
    public void onStart(Host host) throws IOException {
        host.enableConnections(connectionTcpPort);
    }

    @Override
    public void onConnectionsEnabled(int port) {

    }

    @Override
    public void onConnectionsDisabled() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onInitializeClient(ConnectionInitializer initializer) {
        initializer.addProducer((ctx, com) -> sendMessage(com, String.format("Hello %s", ctx.get(String.class, "clientName"))));

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
    public void onClientAuthenticated(ConnectionContext context, Communicator communicator) {
        log(String.format("Client has been authenticated: %s", communicator.getEndpoint()));
    }

    @Override
    public void onClientInitialized(ConnectionContext context, Communicator communicator) {

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