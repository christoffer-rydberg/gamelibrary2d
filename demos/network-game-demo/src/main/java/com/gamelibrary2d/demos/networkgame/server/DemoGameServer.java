package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.common.io.BitParser;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.common.io.Read;
import com.gamelibrary2d.demos.networkgame.common.ClientMessages;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.common.NetworkConstants;
import com.gamelibrary2d.demos.networkgame.common.ServerMessages;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerObject;
import com.gamelibrary2d.demos.networkgame.server.objects.ServerPlayer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.initialization.*;
import com.gamelibrary2d.network.server.ServerHandshakeConfiguration;
import com.gamelibrary2d.network.common.server.Server;
import com.gamelibrary2d.network.common.server.ServerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;

public class DemoGameServer implements ServerContext {
    public final static float UPDATES_PER_SECOND = 30;
    private final static int STREAM_UPDATE_RATE = 3;
    private final static float STREAMS_PER_SECOND = UPDATES_PER_SECOND / STREAM_UPDATE_RATE;
    private final BitParser bitParser = new BitParser(ByteBuffer.wrap(new byte[NetworkConstants.UPDATE_BUFFER_BYTE_SIZE]));

    private final ServerState state = new ServerState();
    private final ClientStateService clientStateService = new ClientStateService();

    private final Server server;
    private final DemoGameLogic gameLogic;
    private final KeyPair keyPair;
    private final DataBuffer decryptionBuffer = new DynamicByteBuffer();

    private int streamCounter;

    private float startCountdown = 10f;
    private float timer;

    public DemoGameServer(Server server) {
        this(server, null);
    }

    public DemoGameServer(Server server, KeyPair keyPair) {
        this.server = server;
        this.keyPair = keyPair;
        this.gameLogic = new DemoGameLogic(this);
    }

    @Override
    public void configureClientAuthentication(CommunicatorInitializer initializer) {
        if (keyPair == null) {
            throw new NullPointerException("Key pair has not been set");
        }

        initializer.addProducer((ctx, com) -> log(String.format("%s: Performing client/server handshake", com.getEndpoint())));

        initializer.addConfig(new ServerHandshakeConfiguration(keyPair));
        initializer.addConsumer(this::validatePassword);
        initializer.addConfig(new UdpConfiguration());
    }

    private boolean validatePassword(CommunicatorInitializationContext context, Communicator communicator, DataBuffer inbox) throws IOException {
        decryptionBuffer.clear();
        communicator.readEncrypted(inbox, decryptionBuffer);
        decryptionBuffer.flip();

        String serverPassword = Read.textWithSizeHeader(decryptionBuffer, StandardCharsets.UTF_8);
        if (!serverPassword.equals("serverPassword123")) {
            throw new IOException("Wrong password");
        }
        return true;
    }

    @Override
    public void configureClientInitialization(CommunicatorInitializer initializer) {
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
    public void onClientAuthenticated(CommunicatorInitializationContext context, Communicator communicator) {
        log(String.format("%s: Client has authenticated", communicator.getEndpoint()));
    }

    @Override
    public void onConnected(Communicator communicator) {
        log(String.format("%s: Client has connected", communicator.getEndpoint()));
    }

    @Override
    public void onClientInitialized(CommunicatorInitializationContext context, Communicator communicator) {
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
    public void onDisconnected(Communicator communicator, boolean pending) {
        log(String.format("%s: Client has disconnected", communicator.getEndpoint()));
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
    public void start() {
        log("Server has started");
    }

    @Override
    public void stop() {
        log("Server has stopped");
    }

    @Override
    public void update(float deltaTime) {
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

        server.sendToAll(ServerMessages.GAME_OVER, false);
    }

    void spawn(ServerObject obj) {
        state.register(obj);
        server.sendToAll(ServerMessages.SPAWN, false);
        server.sendToAll(obj.getObjectIdentifier(), false);
        server.sendToAll(obj, false);
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
        server.sendToAll(ServerMessages.DESTROY, false);
        server.sendToAll(obj.getId(), false);

        if (obj instanceof ServerPlayer) {
            if (allPlayersAreDead()) {
                gameLogic.endGame();
            }
        }
    }

    private void updateClients() {
        server.sendToAll(ServerMessages.UPDATE, true);

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

        server.sendToAll(bitParser.getByteBuffer().array(), 0, bytesToSend, true);
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void log(String message, Throwable e) {
        System.out.println(message);
        e.printStackTrace();
    }
}
