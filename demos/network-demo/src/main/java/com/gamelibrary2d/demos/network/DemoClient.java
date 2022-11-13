package com.gamelibrary2d.demos.network;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.client.AbstractClient;
import com.gamelibrary2d.network.client.RemoteServer;
import com.gamelibrary2d.network.common.events.CommunicatorDisconnectedEvent;
import com.gamelibrary2d.network.common.exceptions.ClientAuthenticationException;
import com.gamelibrary2d.network.common.exceptions.ClientInitializationException;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class DemoClient extends AbstractClient {
    private final UpdateLoop updateLoop;
    private volatile boolean disconnected;

    DemoClient() {
        updateLoop = new UpdateLoop(this::update, 10);
    }

    private Communicator connectCommunicator() throws ExecutionException, InterruptedException {
        return new RemoteServer("localhost", 4444).connect().get();
    }

    void run() {
        try {
            Communicator communicator = connectCommunicator();
            setCommunicator(communicator);
            communicator.addDisconnectedListener(this::onDisconnected);
            sendMessage("What do you call a guy with a rubber toe?");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect communicator");
            e.printStackTrace();
            return;
        } catch (ClientAuthenticationException e) {
            System.err.println("Failed to authenticate client");
            e.printStackTrace();
            return;
        } catch (ClientInitializationException e) {
            System.err.println("Failed to initialize client");
            e.printStackTrace();
            return;
        }

        updateLoop.run();
    }

    private void update(float deltaTime) {
        if (disconnected) {
            readIncoming(); // Read and handle any last messages
            updateLoop.stop();
        } else {
            readIncoming();
            sendOutgoing();
        }
    }

    private void sendMessage(String message) {
        Communicator communicator = getCommunicator();
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        communicator.getOutgoing().putInt(bytes.length);
        communicator.getOutgoing().put(bytes);
    }

    @Override
    protected void initialize(CommunicatorInitializer initializer) {

    }

    @Override
    protected void onMessage(DataBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes, 0, length);
        String message = new String(bytes, StandardCharsets.UTF_8);
        System.out.printf("Server: %s%n", message);
        sendMessage("Roberto.");
    }

    private void onDisconnected(CommunicatorDisconnectedEvent event) {
        disconnected = true;
    }
}