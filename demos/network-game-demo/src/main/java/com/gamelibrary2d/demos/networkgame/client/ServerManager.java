package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.io.Write;
import com.gamelibrary2d.demos.networkgame.server.DemoGameServer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.client.RemoteServer;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.initialization.UdpConfiguration;
import com.gamelibrary2d.network.client.ClientHandshakeConfiguration;
import com.gamelibrary2d.network.client.LocalServer;
import com.gamelibrary2d.network.server.NetworkServer;
import java.io.IOException;
import java.security.KeyPair;

public class ServerManager {
    private final KeyPair keyPair;

    public ServerManager(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public HostedServer hostLocalServer() {
        LocalServer server = new LocalServer(new DemoGameServer());
        return new HostedServer(server, server, DemoGameServer.UPDATES_PER_SECOND);
    }

    public HostedServer hostNetworkServer(String host, int tcpPort) {
        NetworkServer server = new NetworkServer(
                host,
                tcpPort,
                new DemoGameServer(keyPair));

        return new HostedServer(server, connectToServer(host, tcpPort), DemoGameServer.UPDATES_PER_SECOND);
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
