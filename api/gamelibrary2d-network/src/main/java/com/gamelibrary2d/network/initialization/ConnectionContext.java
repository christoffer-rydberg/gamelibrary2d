package com.gamelibrary2d.network.initialization;

import java.util.HashMap;
import java.util.Map;

public class ConnectionContext {

    private final Map<Object, Object> register = new HashMap<>();

    /**
     * Registers an object with the specified key.
     */
    public void register(Object key, Object obj) {
        register.put(key, obj);
    }

    /**
     * Registers an object with its {@link Object#getClass() runtime class} as key.
     */
    public void register(Object value) {
        register(value.getClass(), value);
    }

    /**
     * Gets the object registered with the specified type as key.
     */
    public <T> T get(Class<T> type) {
        return get(type, type);
    }

    /**
     * Gets the object of the specified type registered with the specified key.
     */
    public <T> T get(Class<T> type, Object key) {
        Object obj = register.get(key);
        if (obj != null && type.isAssignableFrom(obj.getClass())) {
            return type.cast(obj);
        }

        return null;
    }
}
