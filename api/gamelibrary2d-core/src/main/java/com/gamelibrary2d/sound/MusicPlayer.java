package com.gamelibrary2d.sound;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.updaters.DurationUpdater;
import com.gamelibrary2d.updaters.InstantUpdater;
import com.gamelibrary2d.updaters.SequentialUpdater;
import com.gamelibrary2d.updaters.Updater;
import com.gamelibrary2d.updates.EmptyUpdate;

public class MusicPlayer {
    private final SoundManager soundManager;
    private final SoundSource[] sources;
    private final SequentialUpdater[] updaters;
    private final int channels;
    private Frame frame;
    private int channel;

    private SoundBuffer activeBuffer;

    public MusicPlayer(Game game, SoundManager soundManager, int channels) {
        this.soundManager = soundManager;
        this.sources = soundManager.createSources(channels);
        this.channels = channels;
        updaters = new SequentialUpdater[channels];
        for (int i = 0; i < channels; ++i) {
            updaters[i] = new SequentialUpdater();
        }

        setFrame(game.getFrame());
        game.addFrameChangedListener(this::setFrame);
    }

    private void setFrame(Frame frame) {
        if (this.frame != frame) {
            this.frame = frame;
            if (frame != null) {
                for (Updater updater : updaters) {
                    if (!updater.isFinished()) {
                        frame.startUpdater(updater);
                    }
                }
            }
        }
    }

    public boolean isPlaying() {
        return activeBuffer != null;
    }

    public boolean isPlaying(Object key) {
        return isPlaying() && soundManager.getBuffer(key) == activeBuffer;
    }

    public boolean stop(float fadeOutTime) {
        if (!isPlaying()) {
            return false;
        }

        SequentialUpdater updater = getUpdater();

        abort(updater);

        // Ensure that the sound buffer has been set
        sources[channel].setSoundBuffer(activeBuffer);
        activeBuffer = null;

        updater.add(SoundUpdaterFactory.createFadeOutUpdater(sources[channel], fadeOutTime, false));

        frame.startUpdater(updater);

        return true;
    }

    public boolean pause(float fadeOutTime) {
        if (!isPlaying()) {
            return false;
        }

        SequentialUpdater updater = getUpdater();

        abort(updater);

        // Ensure that the sound buffer has been set
        sources[channel].setSoundBuffer(activeBuffer);
        activeBuffer = null;

        updater.add(SoundUpdaterFactory.createFadeOutUpdater(sources[channel], fadeOutTime, true));

        frame.startUpdater(updater);

        moveToNextChannel();

        return true;
    }

    public boolean resume(Object key, float volume, boolean looping, float fadeInTime) {
        return canResume(key) && resume(volume, looping, fadeInTime);
    }

    public boolean resume(float volume, boolean looping, float fadeInTime) {
        if (!canResume()) {
            return false;
        }

        boolean stopped = stop(fadeInTime / 2);

        moveToPreviousChannel();

        SoundSource source = sources[channel];
        SequentialUpdater updater = getUpdater();

        abort(updater);

        activeBuffer = source.getSoundBuffer();

        source.setLooping(looping);

        if (stopped) {
            fadeInTime /= 2;
            updater.add(new DurationUpdater(fadeInTime, new EmptyUpdate()));
        }

        updater.add(SoundUpdaterFactory.createFadeInUpdater(source, volume, fadeInTime));

        frame.startUpdater(updater);

        return true;
    }

    public void play(Object key, float volume, boolean looping, float fadeInTime, boolean pausePrevious) {
        boolean stopped = pausePrevious ? pause(fadeInTime / 2) : stop(fadeInTime / 2);

        SequentialUpdater updater = getUpdater();

        activeBuffer = soundManager.getBuffer(key);
        SoundSource source = sources[channel];
        source.setLooping(looping);
        updater.add(new InstantUpdater(dt -> source.setSoundBuffer(activeBuffer)));

        updater.add(SoundUpdaterFactory.createFadeInUpdater(sources[channel], volume,
                stopped ? fadeInTime / 2 : fadeInTime));

        frame.startUpdater(updater);
    }

    private boolean canResume() {
        moveToPreviousChannel();
        SoundSource source = sources[channel];
        SoundBuffer buffer = source.getSoundBuffer();
        moveToNextChannel();
        return buffer != null;
    }

    private boolean canResume(Object key) {
        SoundBuffer soundBuffer = soundManager.getBuffer(key);
        moveToPreviousChannel();
        SoundSource source = sources[channel];
        SoundBuffer buffer = source.getSoundBuffer();
        moveToNextChannel();
        return buffer != null && buffer == soundBuffer;
    }

    private SoundSource getActiveSource() {
        return activeBuffer != null ? sources[channel] : null;
    }

    public float getVolume() {
        SoundSource source = getActiveSource();
        return source != null ? source.getVolume() : 0f;
    }

    public void setVolume(float volume) {
        SoundSource source = getActiveSource();
        if (source != null) {
            abort(getUpdater());
            source.setVolume(volume);
        }
    }

    public void setVolume(float volume, float fadeInTime) {
        SoundSource source = getActiveSource();
        if (source != null) {
            SequentialUpdater updater = getUpdater();
            abort(updater);
            updater.add(SoundUpdaterFactory.createVolumeUpdater(source, volume, fadeInTime));
            frame.startUpdater(updater);
        }
    }

    public boolean isLooping() {
        SoundSource source = getActiveSource();
        return source != null && source.isLooping();
    }

    public void setLooping(boolean looping) {
        SoundSource source = getActiveSource();
        if (source != null) {
            source.setLooping(looping);
        }
    }

    private SequentialUpdater getUpdater() {
        return updaters[channel];
    }

    private void moveToNextChannel() {
        channel = (channel + 1) % channels;
    }

    private void moveToPreviousChannel() {
        channel = (channel + channels - 1) % channels;
    }

    private void abort(SequentialUpdater updater) {
        updater.clear();
    }
}
