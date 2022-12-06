package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.demos.networkgame.server.DemoServerLogic;
import com.gamelibrary2d.io.Write;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.ClientHandshakeConfiguration;
import com.gamelibrary2d.network.client.LocalServer;
import com.gamelibrary2d.network.client.RemoteServer;
import com.gamelibrary2d.network.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.initialization.UdpConfiguration;
import com.gamelibrary2d.network.server.NetworkServer;

import java.io.IOException;
import java.security.KeyPair;

public class ServerManager {
    private final KeyPair keyPair;

    public ServerManager(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public HostedServer hostLocalServer() {
        LocalServer server = new LocalServer(new DemoServerLogic());
        return new HostedServer(server, server, DemoServerLogic.UPDATES_PER_SECOND);
    }

    public HostedServer hostNetworkServer(String host, int tcpPort) {
        NetworkServer server = new NetworkServer(
                host,
                tcpPort,
                new DemoServerLogic(keyPair));

        return new HostedServer(server, connectToServer(host, tcpPort), DemoServerLogic.UPDATES_PER_SECOND);
    }

    public RemoteServer connectToServer(String host, int tcpPort) {
        RemoteServer server = new RemoteServer(host, tcpPort);
        server.addAuthentication(this::configureAuthentication);
        return server;
    }

    private void configureAuthentication(CommunicatorInitializer initializer) {
        initializer.addConfig(new ClientHandshakeConfiguration());
        initializer.addProducer(this::sendPassword);
        initializer.addConfig(new UdpConfiguration());
    }

    private void sendPassword(CommunicatorInitializationContext ctx, Communicator com) throws IOException {
        com.writeEncrypted(b -> Write.textWithSizeHeader("serverPassword123", b));
    }
}
