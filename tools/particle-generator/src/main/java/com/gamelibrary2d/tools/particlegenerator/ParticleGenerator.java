package com.gamelibrary2d.tools.particlegenerator;

import com.gamelibrary2d.AbstractGame;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.lwjgl.GlfwWindow;
import com.gamelibrary2d.lwjgl.Lwjgl_Framework;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.resources.Surfaces;
import com.gamelibrary2d.tools.particlegenerator.resources.Textures;

import java.io.IOException;

public class ParticleGenerator extends AbstractGame {
    public final static Disposer GLOBAL_DISPOSER = new DefaultDisposer();

    private final Frame frame;

    ParticleGenerator() {
        super(new Lwjgl_Framework());
        frame = new ParticleFrame(this);
        registerDisposal(GLOBAL_DISPOSER);
    }

    public static void main(String[] args) throws IOException {
        new ParticleGenerator().start(GlfwWindow.createWindowed("Particle Generator", 1280, 900));
    }

    @Override
    public void update(float deltaTime) {
        float fps = 1f / deltaTime;
        getWindow().setTitle("Particle Generator (" + fps + ")");
        super.update(deltaTime);
    }

    @Override
    protected void onStart() throws IOException {
        Fonts.create(this);
        Textures.create(this);
        Surfaces.create(this);
        setFrame(frame);
    }

    @Override
    protected void onExit() {

    }
}