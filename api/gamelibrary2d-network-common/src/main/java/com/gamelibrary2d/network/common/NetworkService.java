package com.gamelibrary2d.network.common;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class NetworkService {
    private final Map<SelectableChannel, ConnectionListener> connectionListeners = new Hashtable<>();
    private final Map<SocketChannel, TcpConnection> tcpConnections = new Hashtable<>();
    private final Map<DatagramChannel, UdpConnection> udpConnections = new Hashtable<>();
    private final ByteBuffer readBuffer;
    private final Thread thread;
    private Selector selector;

    public NetworkService() {
        this(8192);
    }

    public NetworkService(int readBufferSize) {
        readBuffer = ByteBuffer.allocateDirect(readBufferSize);
        thread = new Thread(this::run);
    }

    public boolean isRunning() {
        return thread != null && thread.isAlive();
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

    public ServerSocketChannelRegistration registerConnectionListener(
            String hostName, int port, SocketChannelConnectedHandler onConnected,
            SocketChannelFailedConnectionHandler onConnectionFailed)
            throws IOException {

        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        try {
            socketChannel.configureBlocking(false);
            socketChannel.socket().bind(new InetSocketAddress(hostName, port));
            connectionListeners.put(socketChannel, new ConnectionListener(onConnected, onConnectionFailed));
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);
            return new ServerSocketChannelRegistration(socketChannel, socketChannel.socket().getLocalPort());
        } catch (IOException e) {
            close(socketChannel);
            throw e;
        }
    }

    public void deregisterConnectionListener(ServerSocketChannelRegistration registration) throws IOException {
        ServerSocketChannel channel = registration.getServerSocketChannel();
        connectionListeners.remove(registration.getServerSocketChannel());
        channel.close();
    }

    public void send(DatagramChannel channel, DataBuffer buffer) throws IOException {
        try {
            UdpConnection udpConnection = udpConnections.get(channel);

            if (udpConnection == null) {
                throw new IOException("No connected UDP communicator");
            }

            if (udpConnection.connectionType == ConnectionType.READ) {
                throw new IOException("UDP connection is read-only");
            }

            DataBuffer byteBuffer = udpConnection.getWriteBuffer();
            synchronized (byteBuffer) {
                int transmissionId = udpConnection.incrementTransmissionId();
                byteBuffer.putBool(true);
                byteBuffer.putInt(transmissionId);
                byteBuffer.putInt(buffer.remaining());
                byteBuffer.put(buffer);
                channel.register(selector, SelectionKey.OP_WRITE, udpConnection);
            }
        } catch (IOException e) {
            disconnect(channel, e);
            throw e;
        }
    }

    public void send(SocketChannel channel, DataBuffer buffer) throws IOException {
        try {
            TcpConnection tcpConnection = tcpConnections.get(channel);
            if (tcpConnection == null) {
                throw new IOException("No connected TCP communicator");
            }
            DataBuffer byteBuffer = tcpConnection.getWriteBuffer();
            synchronized (byteBuffer) {
                tcpConnection.incrementTransmissionId();
                byteBuffer.putBool(false);
                byteBuffer.putInt(buffer.remaining());
                byteBuffer.put(buffer);
                channel.register(selector, SelectionKey.OP_WRITE, tcpConnection);
            }
        } catch (IOException e) {
            disconnect(channel, e);
            throw e;
        }
    }

    public void connect(String hostname, int port, SocketChannelConnectedHandler onConnected,
                        SocketChannelFailedConnectionHandler onConnectionFailed) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(hostname, port));
        connectionListeners.put(channel, new ConnectionListener(onConnected, onConnectionFailed));
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void connect(SocketChannel socketChannel, Communicator communicator,
                        ChannelDisconnectedHandler disconnectedHandler) {
        try {
            TcpConnection tcpConnection = new TcpConnection(communicator, disconnectedHandler);
            tcpConnections.put(socketChannel, tcpConnection);
            socketChannel.register(selector, SelectionKey.OP_READ, tcpConnection);
        } catch (IOException e) {
            disconnect(socketChannel, e);
        }
    }

    public void connect(DatagramChannel datagramChannel, Communicator communicator, ConnectionType connectionType, int hostPort, ChannelDisconnectedHandler disconnectedHandler) {
        UdpConnection udpConnection = new UdpConnection(communicator, connectionType, disconnectedHandler);
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

    public void closeAfterLastScheduledSend(SocketChannel channel) {
        TcpConnection tcpConnection = tcpConnections.get(channel);
        if (tcpConnection != null) {
            DataBuffer byteBuffer = tcpConnection.getWriteBuffer();
            synchronized (byteBuffer) {
                tcpConnection.scheduleCloseConnection();
                if (tcpConnection.shouldClose()) {
                    disconnect(channel);
                }
            }
        }
    }

    public void disconnect(DatagramChannel datagramChannel) {
        disconnect(datagramChannel, null);
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
            closeConnection();
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = null;
        ConnectionListener connectionListener = connectionListeners.get(serverSocketChannel);
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

    private void close(Closeable channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(SelectionKey key) {
        Object connection = key.attachment();
        if (connection instanceof TcpConnection) {
            writeTcp(key, (TcpConnection) connection);
        } else if (connection instanceof UdpConnection) {
            writeUdp(key, (UdpConnection) connection);
        }
    }

    private void writeTcp(SelectionKey key, TcpConnection tcpConnection) {
        SocketChannel channel = (SocketChannel) key.channel();
        DataBuffer byteBuffer = tcpConnection.getWriteBuffer();
        try {
            synchronized (byteBuffer) {
                byteBuffer.flip();
                channel.write(byteBuffer.internalByteBuffer());
                int remaining = byteBuffer.remaining();
                if (remaining > 0) {
                    int position = byteBuffer.position();
                    byteBuffer.clear();
                    byteBuffer.put(byteBuffer.array(), position, remaining);
                    key.interestOps(SelectionKey.OP_WRITE);
                } else {
                    byteBuffer.clear();
                    key.interestOps(SelectionKey.OP_READ);
                }

                tcpConnection.incrementProcessedTransmissions();
                if (tcpConnection.shouldClose()) {
                    disconnect(channel);
                }
            }
        } catch (IOException e) {
            disconnect(channel, e);
        }
    }

    private void writeUdp(SelectionKey key, UdpConnection udpConnection) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        DataBuffer byteBuffer = udpConnection.getWriteBuffer();
        try {
            synchronized (byteBuffer) {
                byteBuffer.flip();
                channel.write(byteBuffer.internalByteBuffer());
                int remaining = byteBuffer.remaining();
                if (remaining > 0) {
                    int position = byteBuffer.position();
                    byteBuffer.clear();
                    byteBuffer.put(byteBuffer.array(), position, remaining);
                    key.interestOps(SelectionKey.OP_WRITE);
                } else {
                    byteBuffer.clear();
                    if (udpConnection.getConnectionType() != ConnectionType.WRITE) {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            disconnect(channel, e);
        }
    }

    private void read(SelectionKey key) {
        Object connection = key.attachment();
        if (connection instanceof TcpConnection) {
            readTcp(key, (TcpConnection) connection);
        } else if (connection instanceof UdpConnection) {
            readUdp(key, (UdpConnection) connection);
        }
    }

    private void readTcp(SelectionKey key, TcpConnection tcpConnection) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            tcpConnection.getCommunicator().addIncoming(tcpConnection.getChannel(), buffer -> {
                while (true) {
                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead > 0) {
                        readBuffer.flip();
                        buffer.put(readBuffer);
                        readBuffer.clear();
                    } else if (bytesRead == 0) {
                        return;
                    } else {
                        disconnect(channel);
                        return;
                    }
                }
            });
        } catch (IOException e) {
            disconnect(channel, e);
        }
    }

    private void readUdp(SelectionKey key, UdpConnection udpConnection) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        try {
            udpConnection.getCommunicator().addIncoming(udpConnection.getChannel(), buffer -> {
                while (true) {
                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead > 0) {
                        readBuffer.flip();
                        buffer.put(readBuffer);
                        readBuffer.clear();
                    } else if (bytesRead == 0) {
                        return;
                    } else {
                        disconnect(channel);
                        return;
                    }
                }
            });
        } catch (IOException e) {
            disconnect(channel, e);
        }
    }

    private void handleConnect(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionListener connectionListener = connectionListeners.remove(channel);
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

    private void disconnect(SocketChannel socketChannel, IOException e) {
        TcpConnection tcpConnection = tcpConnections.remove(socketChannel);
        close(socketChannel);
        if (tcpConnection != null) {
            tcpConnection.onDisconnected(e);
        }
    }

    private void disconnect(DatagramChannel datagramChannel, IOException e) {
        UdpConnection udpConnection = udpConnections.remove(datagramChannel);
        close(datagramChannel);
        if (udpConnection != null) {
            udpConnection.onDisconnected(e);
        }
    }

    private void closeConnection() {
        if (selector != null) {
            close(selector);
            readBuffer.clear();

            for (Closeable channel : connectionListeners.keySet()) {
                close(channel);
            }
            connectionListeners.clear();

            for (Closeable socketChannel : tcpConnections.keySet()) {
                close(socketChannel);
            }
            tcpConnections.clear();

            for (Closeable datagramChannel : udpConnections.keySet()) {
                close(datagramChannel);
            }
            udpConnections.clear();
        }
    }

    private static class ConnectionListener {
        private final SocketChannelConnectedHandler connectedHandler;
        private final SocketChannelFailedConnectionHandler connectionFailedHandler;

        ConnectionListener(SocketChannelConnectedHandler connectedHandler,
                           SocketChannelFailedConnectionHandler connectionFailedHandler) {
            this.connectedHandler = connectedHandler;
            this.connectionFailedHandler = connectionFailedHandler;
        }

        void onConnected(SocketChannel socketChannel) throws IOException {
            connectedHandler.onConnected(socketChannel);
        }

        void onConnectionFailed(String endpoint, IOException exception) {
            connectionFailedHandler.onConnectionFailed(endpoint, exception);
        }
    }

    private abstract static class AbstractConnection {
        private final Communicator communicator;
        private final ChannelDisconnectedHandler disconnectedHandler;
        private final DataBuffer writeBuffer = new DynamicByteBuffer();
        private final int channel;

        int prevTransmissionId;

        AbstractConnection(Communicator communicator, ChannelDisconnectedHandler disconnectedHandler,
                           int channel) {
            this.communicator = communicator;
            this.disconnectedHandler = disconnectedHandler;
            this.channel = channel;
        }

        int incrementTransmissionId() {
            return ++prevTransmissionId;
        }

        public void onDisconnected(IOException e) {
            disconnectedHandler.onDisconnected(e);
        }

        public Communicator getCommunicator() {
            return communicator;
        }

        DataBuffer getWriteBuffer() {
            return writeBuffer;
        }

        int getChannel() {
            return channel;
        }
    }

    private static class TcpConnection extends AbstractConnection {

        private int finalTransmission = -1;
        private int processedTransmissions;

        TcpConnection(Communicator communicator, ChannelDisconnectedHandler disconnectedHandler) {
            super(communicator, disconnectedHandler, 0);
        }

        void incrementProcessedTransmissions() {
            ++processedTransmissions;
        }

        void scheduleCloseConnection() {
            finalTransmission = prevTransmissionId;
        }

        boolean shouldClose() {
            return finalTransmission != -1 && processedTransmissions >= finalTransmission;
        }
    }

    private static class UdpConnection extends AbstractConnection {
        private final ConnectionType connectionType;

        UdpConnection(Communicator communicator, ConnectionType connectionType,
                      ChannelDisconnectedHandler disconnectedHandler) {
            super(communicator, disconnectedHandler, 1);
            this.connectionType = connectionType;
        }

        ConnectionType getConnectionType() {
            return connectionType;
        }
    }
}