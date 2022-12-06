package com.gamelibrary2d.demos.networkgame.server;

import com.gamelibrary2d.demos.networkgame.server.objects.ServerObject;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Serializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerState implements Serializable {
    private final Map<Integer, ServerObject> objects = new HashMap<>();
    private int lastObjectId = -1;

    public void register(ServerObject obj) {
        int key = ++lastObjectId;
        objects.put(key, obj);
        obj.onRegistered(key);
    }

    public void deregister(ServerObject obj) {
        objects.remove(obj.getId());
    }

    public Collection<ServerObject> getAll() {
        return objects.values();
    }

    public void clear() {
        lastObjectId = -1;
        objects.clear();
    }

    @Override
    public void serialize(DataBuffer buffer) {
        Collection<ServerObject> objects = this.objects.values();
        buffer.putInt(objects.size());
        for (ServerObject obj : objects) {
            buffer.put(obj.getObjectIdentifier());
            obj.serialize(buffer);
        }
    }
}
