package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.demos.networkgame.server.objects.ServerPlayer;
import com.gamelibrary2d.network.Communicator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class ClientStateService {

    private final Hashtable<Communicator, ClientState> clientStates = new Hashtable<>();

    public void put(ClientState state) {
        clientStates.put(state.getCommunicator(), state);
    }

    public ClientState get(Communicator communicator) {
        return clientStates.get(communicator);
    }

    public List<ClientState> getAll() {
        return new ArrayList<>(clientStates.values());
    }

    public ClientState remove(Communicator communicator) {
        return clientStates.remove(communicator);
    }

    public int size() {
        return clientStates.size();
    }

    public boolean allReady() {
        for (ClientState state : clientStates.values()) {
            if (!state.isReady()) {
                return false;
            }
        }

        return true;
    }

    public List<ServerPlayer> getPlayers() {
        List<ServerPlayer> players = new ArrayList<>();
        for (ClientState state : clientStates.values()) {
            players.addAll(state.getPlayers());
        }
        return players;
    }
}
