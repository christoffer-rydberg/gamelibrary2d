package com.gamelibrary2d.network.initialization;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.network.Communicator;
import com.gamelibrary2d.network.ConnectionType;
import com.gamelibrary2d.network.NetworkCommunicator;

import java.io.IOException;

public class UdpConfiguration implements TaskConfiguration {

    @Override
    public void addTasks(ConnectionInitializer initializer) {
        initializer.addProducer(UdpConfiguration::enableUdp);
        initializer.addConsumer(UdpConfiguration::connectUdp);
    }

    private static void enableUdp(CommunicatorInitializationContext ctx, Communicator com) throws IOException {
        NetworkCommunicator networkCommunicator = (NetworkCommunicator) com;
        int localPort = networkCommunicator.enableUdp(ConnectionType.READ, 0);
        com.writeEncrypted(b -> b.putInt(localPort));
    }

    private static boolean connectUdp(CommunicatorInitializationContext ctx, Communicator com, DataBuffer inbox) throws IOException {
        NetworkCommunicator networkCommunicator = (NetworkCommunicator) com;
        DataBuffer decryptionBuffer = new DynamicByteBuffer();
        decryptionBuffer.clear();
        com.readEncrypted(inbox, decryptionBuffer);
        decryptionBuffer.flip();

        int udpPort = decryptionBuffer.getInt();

        networkCommunicator.connectUdp(udpPort);

        return true;
    }
}
