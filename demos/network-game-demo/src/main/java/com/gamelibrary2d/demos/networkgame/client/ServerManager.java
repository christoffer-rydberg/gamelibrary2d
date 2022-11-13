package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.io.Write;
import com.gamelibrary2d.common.updating.UpdateLoop;
import com.gamelibrary2d.demos.networkgame.server.DemoGameServer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.client.RemoteServer;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializationContext;
import com.gamelibrary2d.network.common.initialization.CommunicatorInitializer;
import com.gamelibrary2d.network.common.initialization.UdpConfiguration;
import com.gamelibrary2d.network.client.ClientHandshakeConfiguration;
import com.gamelibrary2d.network.client.LocalServer;
import com.gamelibrary2d.network.server.DefaultNetworkServer;
import java.io.IOException;
import java.security.KeyPair;
import java.util.concurrent.Future;

public class ServerManager {
    private final KeyPair keyPair;

    public ServerManager(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public HostedServer hostLocalServer() {
        LocalServer localServer = new LocalServer(DemoGameServer::new);

        return new HostedServer(
                () -> startLocalServer(localServer),
                () -> stopLocalServer(localServer),
                localServer,
                new UpdateLoop(localServer::update, DemoGameServer.UPDATES_PER_SECOND)
        );
    }

    public HostedServer hostNetworkServer(String host, int tcpPort) {
        DefaultNetworkServer server = new DefaultNetworkServer(
                host,
                tcpPort,
                s -> new DemoGameServer(s, keyPair));

        return new HostedServer(
                () -> startNetworkServer(server),
                () -> stopNetworkServer(server),
                () -> connectToServer(host, tcpPort),
                new UpdateLoop(server::update, DemoGameServer.UPDATES_PER_SECOND)
        );
    }

    private void startLocalServer(LocalServer server) {
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopLocalServer(LocalServer server) {
        try {
            server.stop();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startNetworkServer(DefaultNetworkServer server) {
        try {
            server.start();
            server.listenForConnections(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopNetworkServer(DefaultNetworkServer server) {
        try {
            server.stop();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Communicator> connectToServer(String host, int tcpPort) {
        RemoteServer server = new RemoteServer(host, tcpPort);
        server.addAuthentication(this::configureAuthentication);
        return server.connect();
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
