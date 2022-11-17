package com.gamelibrary2d.network.common.initialization;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.network.common.Communicator;
import com.gamelibrary2d.network.common.ConnectionType;
import com.gamelibrary2d.network.common.NetworkCommunicator;

import java.io.IOException;

public class UdpConfiguration implements TaskConfiguration {

    @Override
    public void addTasks(CommunicatorInitializer initializer) {
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
