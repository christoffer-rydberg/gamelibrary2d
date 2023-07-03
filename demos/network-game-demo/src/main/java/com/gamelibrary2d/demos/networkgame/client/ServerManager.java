package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.demos.networkgame.server.ClientAuthenticator;
import com.gamelibrary2d.demos.networkgame.server.DemoServerLogic;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.Write;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.client.ClientHandshakeConfiguration;
import com.gamelibrary2d.network.client.LocalServer;
import com.gamelibrary2d.network.client.NetworkServerConnectionFactory;
import com.gamelibrary2d.network.initialization.ConnectionContext;
import com.gamelibrary2d.network.initialization.ConnectionInitializer;
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
        return new HostedServer(server, DemoServerLogic.UPDATES_PER_SECOND, disposer -> server);
    }

    public HostedServer hostNetworkServer(String host, int tcpPort) {
        NetworkServer server = new NetworkServer(
                host,
                new DemoServerLogic(tcpPort),
                new ClientAuthenticator(keyPair));

        return new HostedServer(
                server,
                DemoServerLogic.UPDATES_PER_SECOND,
                disposer -> createConnectionFactory(host, tcpPort, disposer));
    }

    public NetworkServerConnectionFactory createConnectionFactory(String host, int tcpPort, Disposer disposer) {
        return new NetworkServerConnectionFactory(host, tcpPort, this::addAuthentication, disposer);
    }

    private void addAuthentication(ConnectionInitializer initializer) {
        initializer.addConfig(new ClientHandshakeConfiguration());
        initializer.addProducer(this::sendPassword);
        initializer.addConfig(new UdpConfiguration());
    }

    private void sendPassword(ConnectionContext ctx, Communicator com) throws IOException {
        com.writeEncrypted(b -> Write.textWithSizeHeader("serverPassword123", b));
    }
}
