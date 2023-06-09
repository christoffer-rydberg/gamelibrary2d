package com.gamelibrary2d.network;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.network.connections.ConnectionService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public abstract class AbstractNetworkCommunicator extends AbstractCommunicator implements NetworkCommunicator {
    private final ConnectionService connectionService;
    private volatile UdpConnection udpConnection;
    private volatile SocketChannel socketChannel;

    protected AbstractNetworkCommunicator(ConnectionService connectionService, int incomingChannels) {
        super(incomingChannels);
        this.connectionService = connectionService;
    }

    protected ConnectionService getConnectionService() {
        return connectionService;
    }

    protected SocketChannel getSocketChannel() {
        return socketChannel;
    }

    protected void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    protected void onSocketChannelDisconnected(IOException error) {
        disconnect(error);
    }

    protected void onDatagramChannelDisconnected(IOException error) {
        if (error != null) {
            disconnect(error);
        }
    }

    @Override
    protected void onDisconnected(Throwable cause) {
        disconnectTcp();
        disableUdp();
    }

    @Override
    protected void send(DataBuffer buffer) throws IOException {
        SocketChannel socketChannel = this.socketChannel;
        if (socketChannel == null)
            throw new IOException("Socket channel not connected");
        connectionService.send(socketChannel, buffer);
    }

    @Override
    public void stream(DataBuffer buffer) throws IOException {
        if (udpConnection != null) {
            switch (udpConnection.connectionType) {
                case WRITE:
                case READ_WRITE:
                    getConnectionService().send(udpConnection.channel, buffer);
                    break;
                case READ:
                    super.stream(buffer);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown UDP connection type: " + udpConnection.connectionType);
            }
        } else {
            super.stream(buffer);
        }
    }

    protected void disconnectTcp() {
        if (socketChannelConnected()) {
            SocketChannel socketChannel = this.socketChannel;
            setSocketChannel(null);
            connectionService.disconnectWhenAllDataIsSent(socketChannel);
        }
    }

    @Override
    public int enableUdp(ConnectionType connectionType, int localPort) throws IOException {
        if (!isConnected()) {
            throw new IOException("Communicator is not connected");
        }

        disableUdp();

        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(localPort));
        InetSocketAddress socketAddress = (InetSocketAddress) channel.getLocalAddress();
        this.udpConnection = new UdpConnection(channel, connectionType);
        return socketAddress.getPort();
    }

    @Override
    public void connectUdp(int hostPort) throws IOException {
        if (udpConnection == null) {
            throw new IOException("UDP has not been enabled");
        }

        connectionService.connect(
                this.udpConnection.channel,
                this,
                this.udpConnection.connectionType,
                hostPort,
                this::onDatagramChannelDisconnected);
    }

    @Override
    public void disableUdp() {
        UdpConnection connection = this.udpConnection;
        if (connection != null) {
            this.udpConnection = null;
            connectionService.disconnect(connection.channel);
        }
    }

    protected boolean socketChannelConnected() {
        return socketChannel != null;
    }

    private static class UdpConnection {
        private final DatagramChannel channel;
        private final ConnectionType connectionType;

        public UdpConnection(DatagramChannel channel, ConnectionType connectionType) {
            this.channel = channel;
            this.connectionType = connectionType;
        }
    }
}