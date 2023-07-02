package com.gamelibrary2d.sound.lwjgl;

import com.gamelibrary2d.disposal.AbstractDisposer;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.sound.SoundManager;
import com.gamelibrary2d.sound.SoundSource;
import com.gamelibrary2d.sound.lwjgl.decoders.AudioDecoder;
import com.gamelibrary2d.sound.lwjgl.decoders.VorbisDecoder;
import org.lwjgl.openal.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DefaultSoundManager extends AbstractDisposer implements SoundManager {
    private final long device;
    private final long context;
    private final Hashtable<Object, DefaultSoundBuffer> soundBuffers = new Hashtable<>();
    private final Hashtable<String, AudioDecoder> decoders = new Hashtable<>();
    private SoundListener listener;

    private DefaultSoundManager(long device, long context) {
        this.device = device;
        this.context = context;
        setDecoder("ogg", new VorbisDecoder());
    }

    public static DefaultSoundManager create(Disposer disposer) {
        long device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        long context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);

        DefaultSoundManager soundManager = new DefaultSoundManager(device, context);
        disposer.registerDisposal(soundManager);
        return soundManager;
    }

    public AudioDecoder getDecoder(String format) {
        return decoders.get(format);
    }

    public void setDecoder(String format, AudioDecoder decoder) {
        decoders.put(format, decoder);
    }

    @Override
    public SoundSource[] createSources(int size) {
        Disposer disposer = new DefaultDisposer(this);
        DefaultSoundSource[] sources = new DefaultSoundSource[size];
        for (int i = 0; i < size; ++i) {
            DefaultSoundSource source = DefaultSoundSource.create(disposer);
            source.setRelative(true);
            sources[i] = source;
        }

        return sources;
    }

    @Override
    public DefaultSoundBuffer getBuffer(Object key) {
        return soundBuffers.get(key);
    }

    @Override
    public void loadBuffer(Object key, InputStream stream, String format) throws IOException {
        AudioDecoder decoder = getDecoder(format);
        if (decoder == null) {
            throw new IOException(String.format("No decoder has been registered for the format '%s'", format));
        }

        soundBuffers.put(key, decoder.decode(stream, this));
    }

    /**
     * Gets the attached {@link SoundListener}. The sound listener is automatically
     * activated/deactivated when it is attached/detached to the sound manager.
     */
    public SoundListener getListener() {
        return this.listener;
    }

    /**
     * Setter for the {@link #getListener() attached sound listener}.
     */
    public void setListener(SoundListener listener) {
        if (this.listener == listener) {
            return;
        }

        if (this.listener != null) {
            this.listener.setActive(false);
        }

        this.listener = listener;

        if (this.listener != null) {
            alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
            this.listener.setActive(true);
        } else {
            alDistanceModel(AL10.AL_NONE);
        }
    }

    @Override
    protected void onDispose() {
        if (context != NULL) {
            alcDestroyContext(context);
        }

        if (device != NULL) {
            alcCloseDevice(device);
        }
    }
}