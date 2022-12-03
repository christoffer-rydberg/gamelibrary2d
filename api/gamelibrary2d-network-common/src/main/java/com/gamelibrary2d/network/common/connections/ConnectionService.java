package com.gamelibrary2d.network.common.connections;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.network.common.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class ConnectionService {
    private final Map<SelectableChannel, InternalConnectionListener> connectionListeners = new Hashtable<>();
    private final Map<SocketChannel, InternalTcpConnection> tcpConnections = new Hashtable<>();
    private final Map<DatagramChannel, InternalUdpConnection> udpConnections = new Hashtable<>();
    private final Thread thread;
    private Selector selector;

    public ConnectionService() {
        thread = new Thread(this::run);
    }

    public boolean isRunning() {
        return thread.isAlive();
    }

    public void start() throws IOException {
        if (!isRunning()) {
            selector = Selector.open();
            thread.start();
        }
    }

    public void stop() throws InterruptedException {
        if (isRunning()) {
            thread.interrupt();
            if (!Thread.currentThread().equals(thread)) {
                thread.join();
            }
        }
    }

    public ConnectionListenerRegistration registerConnectionListener(
            String hostName, int port, SocketChannelConnectedHandler onConnected,
            SocketChannelFailedConnectionHandler onConnectionFailed)
            throws IOException {

        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        try {
            socketChannel.configureBlocking(false);
            socketChannel.socket().bind(new InetSocketAddress(hostName, port));

            ConnectionListenerRegistration registration = new ConnectionListenerRegistration(socketChannel, socketChannel.socket().getLocalPort());
            connectionListeners.put(socketChannel, new InternalConnectionListener(onConnected, onConnectionFailed));
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
            return registration;
        } catch (IOException e) {
            close(socketChannel);
            throw e;
        }
    }

    public void deregisterConnectionListener(ConnectionListenerRegistration registration) throws IOException {
        ServerSocketChannel channel = registration.getServerSocketChannel();
        connectionListeners.remove(registration.getServerSocketChannel());
        channel.close();
    }

    public void send(DatagramChannel channel, DataBuffer buffer) throws IOException {
        InternalUdpConnection udpConnection = udpConnections.get(channel);
        if (udpConnection == null) {
            throw new IOException("No connected UDP communicator");
        }

        try {
            udpConnection.addOutgoing(selector, buffer);
        } catch (IOException e) {
            udpConnection.disconnect(e);
            throw e;
        }
    }

    public void send(SocketChannel channel, DataBuffer buffer) throws IOException {
        InternalTcpConnection tcpConnection = tcpConnections.get(channel);
        if (tcpConnection == null) {
            throw new IOException("No connected TCP communicator");
        }

        try {
            tcpConnection.addOutgoing(selector, buffer);
        } catch (IOException e) {
            tcpConnection.disconnect(e);
            throw e;
        }
    }

    public void connect(String hostname, int port, SocketChannelConnectedHandler onConnected,
                        SocketChannelFailedConnectionHandler onConnectionFailed) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(hostname, port));
        connectionListeners.put(channel, new InternalConnectionListener(onConnected, onConnectionFailed));
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void connect(SocketChannel socketChannel, Communicator communicator,
                        ChannelDisconnectedHandler disconnectedHandler) {
        InternalByteChannelReader dataReader = new InternalByteChannelReader(socketChannel);
        InternalTcpConnection tcpConnection = new InternalTcpConnection(dataReader, socketChannel, communicator, exception -> {
            tcpConnections.remove(socketChannel);
            disconnectedHandler.onDisconnected(exception);
        });

        tcpConnections.put(socketChannel, tcpConnection);

        try {
            socketChannel.register(selector, SelectionKey.OP_READ, tcpConnection);
        } catch (IOException e) {
            disconnect(socketChannel, e);
        }
    }

    public void connect(DatagramChannel datagramChannel, Communicator communicator, ConnectionType connectionType, int hostPort, ChannelDisconnectedHandler disconnectedHandler) {
        InternalByteChannelReader dataReader = new InternalByteChannelReader(datagramChannel);

        InternalUdpConnection udpConnection = new InternalUdpConnection(dataReader, datagramChannel, communicator, connectionType, exception -> {
            udpConnections.remove(datagramChannel);
            disconnectedHandler.onDisconnected(exception);
        });

        udpConnections.put(datagramChannel, udpConnection);

        try {
            datagramChannel.connect(new InetSocketAddress(communicator.getEndpoint(), hostPort));
            datagramChannel.configureBlocking(false);
            if (connectionType != ConnectionType.WRITE) {
                datagramChannel.register(selector, SelectionKey.OP_READ, udpConnection);
            }
        } catch (IOException e) {
            disconnect(datagramChannel);
        }
    }

    public void disconnect(SocketChannel socketChannel) {
        disconnect(socketChannel, null);
    }

    private void disconnect(SocketChannel socketChannel, IOException e) {
        InternalTcpConnection tcpConnection = tcpConnections.get(socketChannel);
        if (tcpConnection != null) {
            tcpConnection.disconnect(e);
        }
    }

    public void disconnect(DatagramChannel datagramChannel) {
        InternalUdpConnection udpConnection = udpConnections.get(datagramChannel);
        if (udpConnection != null) {
            udpConnection.disconnect();
        }
    }

    public void closeAfterLastScheduledSend(SocketChannel channel) {
        InternalTcpConnection tcpConnection = tcpConnections.get(channel);
        if (tcpConnection != null) {
            tcpConnection.closeAfterScheduledWrite();
        }
    }

    private void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int selectedKeys = selector.selectNow();
                if (selectedKeys > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid())
                            continue;

                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isWritable()) {
                            write(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isConnectable()) {
                            handleConnect(key);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void handleConnect(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        InternalConnectionListener connectionListener = connectionListeners.remove(channel);
        try {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            connectionListener.onConnected(channel);
        } catch (IOException e) {
            try {
                String hostName = channel.socket().getInetAddress().getCanonicalHostName();
                connectionListener.onConnectionFailed(hostName, e);
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = null;
        InternalConnectionListener connectionListener = connectionListeners.get(serverSocketChannel);
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            connectionListener.onConnected(socketChannel);
        } catch (IOException e) {
            String hostName = socketChannel.socket().getInetAddress().getCanonicalHostName();
            connectionListener.onConnectionFailed(hostName, e);
            close(socketChannel);
        }
    }

    private void write(SelectionKey key) {
        InternalConnection connection = (InternalConnection) key.attachment();
        try {
            connection.sendOutgoing(key);
        } catch (IOException e) {
            connection.disconnect(e);
        }
    }

    private void read(SelectionKey key) {
        InternalConnection connection = (InternalConnection) key.attachment();
        try {
            if (connection.readIncoming() == -1) {
                connection.disconnect();
            }
        } catch (IOException e) {
            connection.disconnect(e);
        }
    }

    private void disconnect() {
        if (selector != null) {
            close(selector);

            for (Closeable channel : connectionListeners.keySet()) {
                close(channel);
            }
            connectionListeners.clear();

            ArrayList<InternalConnection> connections = new ArrayList<>(tcpConnections.size() + udpConnections.size());
            connections.addAll(tcpConnections.values());
            connections.addAll((udpConnections.values()));

            tcpConnections.clear();
            udpConnections.clear();

            for (InternalConnection connection : connections) {
                connection.disconnect();
            }
        }
    }

    private void close(Closeable channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}