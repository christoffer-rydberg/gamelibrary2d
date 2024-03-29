package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.demos.networkgame.common.ClientMessages;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.common.NetworkConstants;
import com.gamelibrary2d.demos.networkgame.common.ServerMessages;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerObject;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerPlayer;
import com.gamelibrary2d.io.BitParser;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
import com.gamelibrary2d.network.server.Host;
import com.gamelibrary2d.network.server.ServerLogic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

public class DemoServerLogic implements ServerLogic {
    public final static float UPDATES_PER_SECOND = 30;
    private final static int STREAM_UPDATE_RATE = 3;
    private final static float STREAMS_PER_SECOND = UPDATES_PER_SECOND / STREAM_UPDATE_RATE;
    private final BitParser bitParser = new BitParser(ByteBuffer.wrap(new byte[NetworkConstants.UPDATE_BUFFER_BYTE_SIZE]));
    private final ServerState state = new ServerState();
    private final ClientStateService clientStateService = new ClientStateService();
    private final DemoGameLogic gameLogic;

    private final int connectionTcpPort;

    private Host host;

    private int streamCounter;

    private float startCountdown = 10f;
    private float timer;

    public DemoServerLogic() {
        this(0);
    }

    public DemoServerLogic(int connectionTcpPort) {
        this.connectionTcpPort = connectionTcpPort;
        this.gameLogic = new DemoGameLogic(this);
    }

    @Override
    public void onInitializeClient(ConnectionInitializer initializer) {
        initializer.send(buffer -> buffer.putFloat(STREAMS_PER_SECOND));
        initializer.send(gameLogic::getGameSettings);
        initializer.receive("requestedPlayers", DataBuffer::getInt);
        initializer.send(state);
    }

    @Override
    public boolean acceptConnection(String endpoint) {
        log(String.format("%s: Accepting client connection", endpoint));
        return true;
    }

    @Override
    public void onConnectionFailed(String endpoint, Exception e) {
        log(String.format("%s: Client connection failed", endpoint), e);
    }

    @Override
    public void onClientAuthenticated(ConnectionContext context, Communicator communicator) {
        log(String.format("%s: Client has authenticated", communicator.getEndpoint()));
    }

    @Override
    public void onConnected(Communicator communicator) {
        log(String.format("%s: Client has connected", communicator.getEndpoint()));
    }

    @Override
    public void onClientInitialized(ConnectionContext context, Communicator communicator) {
        log(String.format("%s: Client has been initialized", communicator.getEndpoint()));

        GameSettings settings = gameLogic.getGameSettings();

        int requestedPlayers = context.get(Integer.class, "requestedPlayers");

        ArrayList<ServerPlayer> players = new ArrayList<>(requestedPlayers);
        for (int i = 0; i < requestedPlayers; ++i) {
            players.add(new ServerPlayer(gameLogic, communicator, settings.getSpaceCraftBounds()));
        }

        ClientState communicatorState = new ClientState(communicator, players);
        clientStateService.put(communicatorState);

        if (gameLogic.isGameOver()) {
            communicator.getOutgoing().put(ServerMessages.GAME_ENDED);
        } else {
            communicatorState.setReady(true);
        }
    }

    @Override
    public void onDisconnected(Communicator communicator, boolean pending, Throwable cause) {
        if (cause != null) {
            log(String.format("%s: Client was disconnected (%s)", communicator.getEndpoint(), cause.getMessage()));
        } else {
            log(String.format("%s: Client was disconnected", communicator.getEndpoint()));
        }

        ClientState clientState = clientStateService.remove(communicator);
        if (clientState != null) {
            for (ServerPlayer player : clientState.getPlayers()) {
                gameLogic.destroy(player);
            }
        }
    }

    @Override
    public void onMessage(Communicator communicator, DataBuffer buffer) {
        byte id = buffer.get();
        switch (id) {
            case ClientMessages.PLAYER_ACCELERATION:
                ServerPlayer player = clientStateService.get(communicator).getPlayer(buffer.getInt());

                player.setAcceleration(buffer.getFloat());

                float goalRotation = buffer.getInt();
                if (goalRotation == Integer.MAX_VALUE) {
                    player.setRotationAcceleration(buffer.getFloat());
                } else {
                    player.setGoalRotation(goalRotation);
                }
                break;
            case ClientMessages.PLAY_AGAIN:
                clientStateService.get(communicator).setReady(true);
                break;
        }
    }

    @Override
    public void onStart(Host host) throws IOException {
        log(String.format("Server has started: %s", host.getHostName()));
        this.host = host;

        log(String.format("Enabling connections on port: %d", connectionTcpPort));
        host.enableConnections(connectionTcpPort);
    }

    @Override
    public void onConnectionsEnabled(int port) {
        log(String.format("Listening for incoming connections on port: %d", port));
    }

    @Override
    public void onConnectionsDisabled() {
        log("Stopped listening for incoming connections");
    }

    @Override
    public void onStop() {
        log("Server has stopped");
    }

    @Override
    public void onUpdate(float deltaTime) {
        if (clientStateService.size() > 0) {
            if (gameLogic.isGameOver()) {
                startCountdown -= deltaTime;
                if (startCountdown <= 0 || clientStateService.allReady()) {
                    startCountdown = 10f;
                    startGame();
                }
            } else {
                // Cap the delta time in case of lag to avoid big movement of objects
                deltaTime = (Math.min(deltaTime, 0.1f));

                timer += deltaTime;

                ++streamCounter;
                gameLogic.update(deltaTime);
                if (streamCounter == STREAM_UPDATE_RATE) {
                    updateClients();
                    streamCounter = 0;
                }
            }
        }
    }

    private void startGame() {
        timer = 0;

        for (ClientState clientState : clientStateService.getAll()) {
            if (!clientState.isReady()) {
                clientState.getCommunicator().disconnect();
            }
        }

        gameLogic.startGame(clientStateService.getPlayers());
    }

    void endGame() {
        state.clear();
        for (ClientState communicatorState : clientStateService.getAll()) {
            communicatorState.setReady(false);
        }

        host.broadcast(ServerMessages.GAME_OVER);
    }

    void spawn(ServerObject obj) {
        state.register(obj);
        host.broadcast(ServerMessages.SPAWN);
        host.broadcast(obj.getObjectIdentifier());
        host.broadcast(obj);
    }

    private boolean allPlayersAreDead() {
        Collection<ServerPlayer> players = clientStateService.getPlayers();
        for (ServerPlayer player : players) {
            if (!player.isDestroyed()) {
                return false;
            }
        }

        return true;
    }

    void destroy(ServerObject obj) {
        state.deregister(obj);
        host.broadcast(ServerMessages.DESTROY);
        host.broadcast(obj.getId());

        if (obj instanceof ServerPlayer) {
            if (allPlayersAreDead()) {
                gameLogic.endGame();
            }
        }
    }

    private void updateClients() {
        DataBuffer streamBuffer = host.getStreamBuffer();

        streamBuffer.put(ServerMessages.UPDATE);

        bitParser.position(NetworkConstants.HEADER_BIT_SIZE);

        bitParser.putInt((int) timer);

        Collection<ServerObject> objects = state.getAll();
        for (ServerObject object : objects) {
            bitParser.putInt(object.getId(), NetworkConstants.OBJECT_ID_BIT_SIZE);
            bitParser.putInt(Math.round(object.getPosition().getX()), NetworkConstants.POS_X_BIT_SIZE);
            bitParser.putInt(Math.round(object.getPosition().getY()), NetworkConstants.POS_Y_BIT_SIZE);

            bitParser.putInt(object.isAccelerating() ? 1 : 0, 1);

            if (object instanceof ServerPlayer) {
                bitParser.putInt(1, 1);
                bitParser.putInt(Math.round(object.getRotation()), NetworkConstants.ROTATION_BIT_SIZE);
            } else {
                bitParser.putInt(0, 1);
            }

            bitParser.putInt(Math.round(object.getDirection()), NetworkConstants.DIRECTION_BIT_SIZE);
        }

        int bitPosition = (int) bitParser.position();
        int bytePosition = bitParser.bytePosition();
        int positionInByte = bitParser.bitIndex();
        int bytesToSend = positionInByte == 0 ? bytePosition : bytePosition + 1;

        bitParser.position(0);
        int bitSize = bitPosition - NetworkConstants.HEADER_BIT_SIZE;
        bitParser.putInt(bitSize, NetworkConstants.HEADER_BIT_SIZE);

        streamBuffer.put(bitParser.getByteBuffer().array(), 0, bytesToSend);
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void log(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }
}
