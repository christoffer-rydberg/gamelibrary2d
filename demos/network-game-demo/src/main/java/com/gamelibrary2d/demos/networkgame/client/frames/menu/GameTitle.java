package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.ResourceReader;
import com.gamelibrary2d.components.denotations.Transformable;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.components.objects.AbstractGameObject;
import com.gamelibrary2d.components.objects.DefaultGameObject;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.demos.networkgame.client.ParticleRendererFactory;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.urls.Images;
import com.gamelibrary2d.demos.networkgame.client.urls.Particles;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.particles.parameters.ParticleSystemParameters;
import com.gamelibrary2d.particles.DefaultParticleSystem;
import com.gamelibrary2d.renderers.ContentRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.DefaultTexture;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.Texture;
import com.gamelibrary2d.updaters.*;
import com.gamelibrary2d.updates.*;

import java.io.IOException;

public class GameTitle extends AbstractGameObject implements Updatable {
    private final Rectangle bounds;
    private final GameObject part1;
    private final GameObject part2;
    private final GameObject part3;
    private final Portal portal;

    private boolean showPortal;

    private GameTitle(Rectangle bounds, GameObject part1, GameObject part2, GameObject part3, Portal portal) {
        this.bounds = bounds;
        this.part1 = part1;
        this.part2 = part2;
        this.part3 = part3;
        this.portal = portal;
        setOpacity(0.75f);
    }

    private static GameObject createPartialTitle(Rectangle titleBounds, Texture titleTexture, float left, float right, Disposer disposer) {
        Rectangle texCoordinates = new Rectangle(left / titleTexture.getWidth(), 0, right / titleTexture.getWidth(), 1f);
        Rectangle bounds = titleBounds.resize(texCoordinates.move(-texCoordinates.getCenterX(), -texCoordinates.getCenterY()));
        ContentRenderer renderer = new SurfaceRenderer<>(Quad.create(bounds, texCoordinates, disposer), titleTexture);
        GameObject partialTitle = new DefaultGameObject<>(renderer);
        partialTitle.setPosition(titleBounds.getLowerX() + texCoordinates.getLowerX() * titleBounds.getWidth() - bounds.getLowerX(), 0);
        return partialTitle;
    }

    public static GameTitle create(Window window, ResourceManager resourceManager, Disposer disposer) throws IOException {
        Texture texture = resourceManager.load(
                Images.MENU_TITLE,
                stream -> DefaultTexture.create(stream, disposer));

        Rectangle bounds = Rectangle.create(1f, texture.getHeight() / texture.getWidth())
                .resize(0.5f * window.getWidth());

        GameObject part1 = createPartialTitle(bounds, texture, 0, 200, disposer);
        GameObject part2 = createPartialTitle(bounds, texture, 200, 825, disposer);
        GameObject part3 = createPartialTitle(bounds, texture, 825, texture.getWidth(), disposer);

        ParticleSystemParameters params = resourceManager.load(
                Particles.PORTAL,
                s -> new ResourceReader().read(s, ParticleSystemParameters::new));

        DefaultParticleSystem portalSystem = DefaultParticleSystem.create(params, ParticleRendererFactory.create(disposer), disposer);
        portalSystem.getParameters().getPositionParameters().scale(0.0005f * window.getWidth());
        portalSystem.getParameters().getParticleParameters().scale(0.0005f * window.getWidth());

        return new GameTitle(bounds, part1, part2, part3, new Portal(portalSystem, part2.getPosition()));
    }

    private Updater suckInMiddlePart(float duration) {
        Updatable suckedIntoPortalUpdate = new AttributeUpdateSet<Transformable>(
                new ScaleUpdate<>(part2, -part2.getScale().getX(), -part2.getScale().getY()),
                new RotationUpdate<>(part2, 360));

        return new DurationUpdater(duration, true, suckedIntoPortalUpdate);
    }

    private Updater moveOuterPartsTogether(float duration) {
        UpdaterSet updater = new ParallelUpdater();
        Updatable moveLeftUpdate = new PositionUpdate<>(part3, -part2.getBounds().getWidth() / 3, 0);
        Updatable moveRightUpdate = new PositionUpdate<>(part1, part2.getBounds().getWidth() / 3, 0);
        updater.add(new DurationUpdater(duration, true, moveRightUpdate));
        updater.add(new DurationUpdater(duration, true, moveLeftUpdate));
        return updater;
    }

    public Updater createIntro() {
        UpdaterSet updater = new SequentialUpdater();

        updater.add(new InstantUpdater(a -> showPortal = true));
        updater.add(new DurationUpdater(3f, new EmptyUpdate()));

        UpdaterSet moveOuterPartsTogether = new SequentialUpdater();
        moveOuterPartsTogether.add(new DurationUpdater(4f, new EmptyUpdate()));
        moveOuterPartsTogether.add(moveOuterPartsTogether(6f));

        UpdaterSet parallelUpdater = new ParallelUpdater();
        parallelUpdater.add(suckInMiddlePart(10f));
        parallelUpdater.add(moveOuterPartsTogether);

        updater.add(parallelUpdater);

        return updater;
    }

    @Override
    protected void onRender(float alpha) {
        if (showPortal) {
            portal.render(alpha);
        }

        part1.render(alpha);
        part2.render(alpha);
        part3.render(alpha);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void update(float deltaTime) {
        if (showPortal) {
            portal.update(deltaTime);
        }
    }

    private static class Portal implements Updatable, Renderable {
        private final Point position;
        private final DefaultParticleSystem particleSystem;
        private float timer;

        private Portal(DefaultParticleSystem particleSystem, Point position) {
            this.particleSystem = particleSystem;
            this.position = new Point(position);
        }

        @Override
        public void update(float deltaTime) {
            timer = particleSystem.emit(position, timer + deltaTime);
            particleSystem.update(deltaTime);
        }

        @Override
        public void render(float alpha) {
            particleSystem.render(alpha);
        }
    }
}
