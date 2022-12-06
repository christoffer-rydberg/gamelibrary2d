package com.gamelibrary2d.sound;

import com.gamelibrary2d.disposal.Disposable;

public interface SoundSource extends Disposable {

    boolean isStopped();

    void play();

    void pause();

    void stop();

    float getVolume();

    void setVolume(float volume);

    SoundBuffer getSoundBuffer();

    void setSoundBuffer(SoundBuffer soundBuffer);

    void setLooping(boolean looping);

    boolean isLooping();
}
