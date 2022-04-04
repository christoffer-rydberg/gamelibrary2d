package com.gamelibrary2d.sound;

public class SoundPlayer {
    private final SoundManager soundManager;
    private final SoundSource[] sources;

    public SoundPlayer(SoundManager soundManager, int channels) {
        this.soundManager = soundManager;
        this.sources = soundManager.createSources(channels);
    }

    public void play(Object key, float volume) {
        SoundBuffer soundBuffer = soundManager.getBuffer(key);
        for (int i = 0; i < sources.length; ++i) {
            SoundSource soundSource = sources[i];
            if (soundSource.isStopped()) {
                soundSource.setVolume(volume);
                soundSource.setSoundBuffer(soundBuffer);
                soundSource.play();
                break;
            }
        }
    }
}
