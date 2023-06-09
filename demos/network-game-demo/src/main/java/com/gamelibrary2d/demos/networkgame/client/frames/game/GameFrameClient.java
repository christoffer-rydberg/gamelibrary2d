package com.gamelibrary2d.demos.networkgame.client.frames.game;

import com.gamelibrary2d.components.frames.FrameClient;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerFactory;
import com.gamelibrary2d.demos.networkgame.client.objects.network.*;
import com.gamelibrary2d.demos.networkgame.common.*;
import com.gamelibrary2d.io.BitParser;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.events.CommunicatorDisconnectedEvent;
import com.gamelibrary2d.network.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;

import java.util.HashMap;
import java.util.Map;

import static com.gamelibrary2d.demos.networkgame.common.ServerMessages.*;

public class GameFrameClient implements FrameClient {
    private final GameFrameManager frameManager;
    private final ControllerFactory controllerFactory;
    private final BitParser bitParser = new BitParser();
    private State state;
    private GameSettings gameSettings;
    private float serverUpdatesPerSecond;
    private Communicator communicator;

    public GameFrameClient(GameFrameManager frameManager, ControllerFactory controllerFactory) {
        this.frameManager = frameManager;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void onInitializeClient(ConnectionInitializer initializer) {
        initializer.addConsumer((ctx, com, inbox) -> {
            serverUpdatesPerSecond = inbox.getFloat();
            return true;
        });

        initializer.addConsumer((ctx, com, inbox) -> {
            this.gameSettings = new GameSettings(inbox);
            return true;
        });

        initializer.addProducer(this::requestPlayers);
        initializer.addConsumer(this::readState);
    }

    private void requestPlayers(CommunicatorInitializationContext context, Communicator communicator) {
        communicator.getOutgoing().putInt(1);
    }

    private boolean readState(CommunicatorInitializationContext context, Communicator communicator, DataBuffer buffer) {
        this.state = new State(buffer);
        return true;
    }

    public void onClientInitialized(Communicator communicator) {
        this.communicator = communicator;
        communicator.addDisconnectedListener(this::onDisconnected);
        frameManager.invokeLater(() -> {
            frameManager.applySettings(gameSettings);
            for (ClientObject obj : state.getObjects().values()) {
                frameManager.spawn(obj);
            }
        });
    }

    private void onDisconnected(CommunicatorDisconnectedEvent event) {
        frameManager.invokeLater(() -> {
            System.err.println("Disconnected from server");
            Throwable cause = event.getCause();
            if (cause != null) {
                cause.printStackTrace();
            }
            frameManager.goToMenu();
        });
    }

    public void onMessage(DataBuffer buffer) {
        byte id = buffer.get();
        switch (id) {
            case ServerMessages.UPDATE:
                update(buffer);
                break;
            case SPAWN:
                spawn(readObject(buffer.get(), buffer));
                break;
            case DESTROY:
                destroy(buffer.getInt());
                break;
            case GAME_OVER:
                gameOver();
                break;
            case GAME_ENDED:
                gameEnded();
                break;
        }
    }

    private void gameOver() {
        state.objects.clear();
        frameManager.gameOver(this::requestNewGame);
    }

    private void gameEnded() {
        state.clear();
        frameManager.gameEnded();
        requestNewGame();
    }

    private void requestNewGame() {
        getCommunicator().getOutgoing().put(ClientMessages.PLAY_AGAIN);
    }

    private ClientObject readObject(byte primaryType, DataBuffer buffer) {
        switch (primaryType) {
            case ObjectTypes.OBSTACLE:
                return new Obstacle(primaryType, this, buffer);
            case ObjectTypes.PLAYER:
                boolean isLocal = buffer.getBool();
                if (isLocal) {
                    return new LocalPlayer(controllerFactory, primaryType, this, buffer);
                } else {
                    return new RemotePlayer(primaryType, this, buffer);
                }
            case ObjectTypes.PORTAL:
                return new Portal(primaryType, this, buffer);
            default:
                throw new IllegalStateException("Unexpected value: " + primaryType);
        }
    }

    private void destroy(int id) {
        ClientObject obj = state.getObjects().remove(id);
        frameManager.destroy(obj);
    }

    private void spawn(ClientObject obj) {
        state.getObjects().put(obj.getId(), obj);
        frameManager.spawn(obj);
    }

    private void update(DataBuffer buffer) {
        bitParser.setByteBuffer(buffer.internalByteBuffer());

        final int headerSize = NetworkConstants.HEADER_BIT_SIZE;
        final int bitSize = bitParser.getInt(headerSize);
        final int totalBitSize = headerSize + bitSize;
        final long endOfUpdate = bitParser.position() + bitSize;

        int byteSize = totalBitSize / 8;
        int positionInByte = totalBitSize - byteSize * 8;
        byteSize = positionInByte == 0 ? byteSize : byteSize + 1;

        int time = bitParser.getInt();

        frameManager.setTime(time);

        while (bitParser.position() < endOfUpdate) {
            int id = bitParser.getInt(NetworkConstants.OBJECT_ID_BIT_SIZE);
            float x = bitParser.getInt(NetworkConstants.POS_X_BIT_SIZE);
            float y = bitParser.getInt(NetworkConstants.POS_Y_BIT_SIZE);

            boolean isAccelerating = bitParser.getInt(1) == 1;

            float rotation = bitParser.getInt(1) == 1
                    ? bitParser.getInt(NetworkConstants.ROTATION_BIT_SIZE)
                    : 0f;

            float direction = bitParser.getInt(NetworkConstants.DIRECTION_BIT_SIZE);
            ClientObject obj = state.getObjects().get(id);
            if (obj != null) {
                obj.setAccelerating(isAccelerating);
                obj.setGoalPosition(x, y);
                obj.setGoalRotation(rotation);
                obj.setGoalDirection(direction);
            }
        }

        if (bitParser.position() != endOfUpdate) {
            throw new IllegalStateException("Wrong bit count in update!");
        }

        buffer.position(buffer.position() + byteSize);
    }

    public float getServerUpdatesPerSecond() {
        return serverUpdatesPerSecond;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    public void disconnect() {
        if (communicator != null) {
            communicator.disconnect();
        }
    }

    private class State {
        private final Map<Integer, ClientObject> objects;

        State(DataBuffer buffer) {
            int size = buffer.getInt();
            objects = new HashMap<>(size);
            for (int i = 0; i < size; ++i) {
                ClientObject obj = readObject(buffer.get(), buffer);
                objects.put(obj.getId(), obj);
            }
        }

        Map<Integer, ClientObject> getObjects() {
            return objects;
        }

        public void clear() {
            objects.clear();
        }
    }
}