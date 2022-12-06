package com.gamelibrary2d.demos.networkgame.client.objects.network;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.demos.networkgame.client.frames.game.GameFrameClient;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.DurationEffect;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.EffectMap;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.InstantEffect;
import com.gamelibrary2d.demos.networkgame.client.objects.network.decoration.RendererMap;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.interpolation.InterpolatableAngle;
import com.gamelibrary2d.interpolation.PositionInterpolator;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.updates.AddScaleUpdate;
import com.gamelibrary2d.updates.Update;

public abstract class AbstractClientObject
        extends AbstractGameObject implements ClientObject, Updatable {

    private final int id;
    private final byte primaryType;
    private final byte secondaryType;
    private final GameFrameClient client;
    private final PositionInterpolator positionInterpolator = new PositionInterpolator(this);
    private final InterpolatableAngle direction = new InterpolatableAngle();

    private final Point particleHotspot = new Point();
    private final Update spawnUpdater = new AddScaleUpdate(1f, this, 1f, 1f);
    private DurationEffect updateEffect;
    private InstantEffect destroyedEffect;
    private boolean accelerating;
    private Renderable renderer;

    protected AbstractClientObject(byte primaryType, GameFrameClient client, DataBuffer buffer) {
        this.primaryType = primaryType;
        this.client = client;
        this.secondaryType = buffer.get();
        id = buffer.getInt();
        setPosition(buffer.getFloat(), buffer.getFloat());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public byte getPrimaryType() {
        return primaryType;
    }

    @Override
    public byte getSecondaryType() {
        return secondaryType;
    }

    @Override
    public Renderable getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderable renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setRenderer(RendererMap rendererMap) {
        rendererMap.setRenderer(this);
    }

    public void setUpdateEffect(DurationEffect updateEffect) {
        this.updateEffect = updateEffect;
    }

    public void setDestroyedEffect(InstantEffect destroyedEffect) {
        this.destroyedEffect = destroyedEffect;
    }

    @Override
    public void spawn(Frame frame) {
        setScale(0f);
        spawnUpdater.reset();
        frame.startUpdate(spawnUpdater);
    }

    @Override
    public void addEffects(EffectMap effectMap) {
        effectMap.setEffects(this);
    }

    @Override
    public Point getParticleHotspot() {
        return particleHotspot;
    }

    public boolean isAccelerating() {
        return accelerating;
    }

    @Override
    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }

    @Override
    public void destroy() {
        if (destroyedEffect != null) {
            destroyedEffect.onUpdate(this);
        }
    }

    @Override
    public void update(float deltaTime) {
        positionInterpolator.update(deltaTime);
        direction.update(deltaTime);

        if (useUpdateEffect()) {
            updateEffect.onUpdate(this, deltaTime);
        }

        if (renderer instanceof Updatable) {
            ((Updatable) renderer).update(deltaTime);
        }
    }

    protected boolean useUpdateEffect() {
        return updateEffect != null;
    }

    @Override
    public float getDirection() {
        return direction.getAngle();
    }

    @Override
    public void setGoalPosition(float x, float y) {
        positionInterpolator.setGoal(x, y, 1f / client.getServerUpdatesPerSecond());
    }

    @Override
    public void setGoalDirection(float direction) {
        this.direction.beginInterpolation(direction, 1f / client.getServerUpdatesPerSecond());
    }

    @Override
    public void setGoalRotation(float rotation) {

    }

    @Override
    public Rectangle getBounds() {
        if (renderer instanceof Bounded)
            return ((Bounded) renderer).getBounds();
        else
            return Rectangle.EMPTY;
    }
}
