package com.gamelibrary2d.sound;

import com.gamelibrary2d.updates.SequentialUpdater;
import com.gamelibrary2d.updates.Update;
import com.gamelibrary2d.updates.AbstractUpdate;

/**
 * The class contains static methods to create various sound {@link Update
 * updaters}.
 */
public class SoundUpdaterFactory {

    /**
     * Creates an updater that fades out a {@link SoundSource}.
     *
     * @param soundSource The sound source.
     * @param duration    The "fade out"-duration in seconds.
     */
    public static Update createFadeOutUpdater(SoundSource soundSource, float duration,
                                              boolean pause) {
        SequentialUpdater updater = new SequentialUpdater(2);
        updater.add(createVolumeUpdater(soundSource, 0, duration));
        if (pause) {
            updater.add(soundSource::pause);
        } else {
            updater.add(soundSource::stop);
        }

        return updater;
    }

    /**
     * Creates an updater that fades in a {@link SoundSource}.
     *
     * @param soundSource The sound source.
     * @param duration    The "fade in"-duration in seconds.
     * @param volume      The volume when faded in.
     */
    public static Update createFadeInUpdater(SoundSource soundSource, float volume, float duration) {
        SequentialUpdater updater = new SequentialUpdater(2);
        updater.add(() -> {
            soundSource.setVolume(0);
            soundSource.play();
        });
        updater.add(createVolumeUpdater(soundSource, volume, duration));
        return updater;
    }

    /**
     * Creates an updater that changes the volume of a {@link SoundSource}.
     *
     * @param soundSource The sound source.
     * @param goalVolume  The goal volume.
     * @param duration    The duration to reach the goal volume, in seconds.
     */
    public static Update createVolumeUpdater(SoundSource soundSource, float goalVolume, float duration) {
        return new VolumeUpdater(soundSource, goalVolume, duration);
    }

    private static class VolumeUpdater extends AbstractUpdate {
        private final SoundSource soundSource;
        private final float goalVolume;
        private float deltaVolume;

        VolumeUpdater(SoundSource soundSource, float goalVolume, float duration) {
            super(duration);
            this.soundSource = soundSource;
            this.goalVolume = goalVolume;
        }

        @Override
        protected void initialize() {
            deltaVolume = (goalVolume - soundSource.getVolume()) / getDuration();
        }

        @Override
        protected void onUpdate(float deltaTime) {
            soundSource.setVolume(soundSource.getVolume() + deltaVolume * deltaTime);
        }
    }
}