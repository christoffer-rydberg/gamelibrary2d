package com.gamelibrary2d.sound;

import com.gamelibrary2d.disposal.Disposer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Manages creation and disposal of sound resources.
 * It also acts as a repository for {@link SoundBuffer sound buffers}.
 */
public interface SoundManager extends Disposer {

    /**
     * Creates an array of {@link SoundSource sound sources}.
     *
     * @param size The number of sources to create.
     */
    SoundSource[] createSources(int size);

    /**
     * Gets the {@link SoundBuffer} that was loaded with the specified key.
     *
     * @param key The key for the {@link SoundBuffer}.
     */
    SoundBuffer getBuffer(Object key);

    /**
     * Loads a sound resource into a {@link SoundBuffer}.
     * The sound buffer will be available by invoking {@link #getBuffer}.
     *
     * @param key    The key when invoking {@link #getBuffer}.
     * @param stream The sound resource stream.
     * @param format The format of the sound resource.
     */
    void loadBuffer(Object key, InputStream stream, String format) throws IOException;

    /**
     * Loads a sound resource into a {@link SoundBuffer}.
     * The sound buffer will be available by invoking {@link #getBuffer}.
     *
     * @param key    The key when invoking {@link #getBuffer}.
     * @param url    The URL of the sound resource.
     * @param format The format of the sound resource.
     */
    default void loadBuffer(Object key, URL url, String format) throws IOException {
        try (InputStream stream = url.openStream()) {
            loadBuffer(key, stream, format);
        }
    }

    /**
     * Loads a sound resource into a {@link SoundBuffer}.
     * The sound buffer will be available by invoking {@link #getBuffer}.
     *
     * @param url    The URL of the sound resource. This will also be the key when invoking {@link #getBuffer}.
     * @param format The format of the sound resource.
     */
    default void loadBuffer(URL url, String format) throws IOException {
        loadBuffer(url, url, format);
    }
}
