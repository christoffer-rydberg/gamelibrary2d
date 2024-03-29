package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.containers.DefaultPanel;
import com.gamelibrary2d.components.containers.Panel;
import com.gamelibrary2d.denotations.Container;
import com.gamelibrary2d.particles.ParticleUpdateParameters;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.properties.BooleanProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;

public class UpdateParametersPanel extends AbstractPanel<GameObject> implements Container<GameObject> {

    private final ParticleSystemModel particleSystem;

    public UpdateParametersPanel(ParticleSystemModel particleSystem) {
        this.particleSystem = particleSystem;
        PanelUtil.stack(this, life(), 0f);
        PanelUtil.stack(this, delay());
        PanelUtil.stack(this, speed());
        PanelUtil.stack(this, endSpeedFactor());
        PanelUtil.stack(this, direction());
        PanelUtil.stack(this, moveFromCenter());
        PanelUtil.stack(this, accelerationX());
        PanelUtil.stack(this, accelerationY());
        PanelUtil.stack(this, centripetalAcceleration());
        PanelUtil.stack(this, tangentialAcceleration());
        PanelUtil.stack(this, individualGravityCenter());
        PanelUtil.stack(this, scale());
        PanelUtil.stack(this, endScale());
        PanelUtil.stack(this, rotation());
        PanelUtil.stack(this, rotatedForward());
        PanelUtil.stack(this, colors());
        PanelUtil.stack(this, endColors());
        PanelUtil.stack(this, alpha());
        PanelUtil.stack(this, endAlpha());
    }

    private ParticleUpdateParameters params() {
        return particleSystem.getSettings().getUpdateParameters();
    }

    private GameObject life() {
        return new PanelElement.Float(
                "Life",
                new FloatProperty(() -> params().getLife(), v -> params().setLife(v)),
                new FloatProperty(() -> params().getLifeVar(), v -> params().setLifeVar(v))
        );
    }

    private GameObject delay() {
        return new PanelElement.Float(
                "Delay",
                new FloatProperty(() -> params().getDelay(), v -> params().setDelay(v)),
                new FloatProperty(() -> params().getDelayVar(), v -> params().setDelayVar(v))
        );
    }

    private GameObject speed() {
        return new PanelElement.Float(
                "Speed",
                new FloatProperty(() -> params().getSpeed(), v -> params().setSpeed(v)),
                new FloatProperty(() -> params().getSpeedVar(), v -> params().setSpeedVar(v))
        );
    }

    private GameObject endSpeedFactor() {
        return new PanelElement.Float(
                "End Speed Factor",
                new FloatProperty(() -> params().getEndSpeedFactor(), v -> params().setEndSpeedFactor(v)),
                new FloatProperty(() -> params().getEndSpeedFactorVar(), v -> params().setEndSpeedFactorVar(v))
        );
    }

    private GameObject moveFromCenter() {
        return new PanelElement.Boolean(
                "Move From Center",
                new BooleanProperty(() -> params().isMovingFromCenter(), v -> params().setMovingFromCenter(v))
        );
    }

    private GameObject direction() {
        return new PanelElement.Float(
                "Direction",
                new FloatProperty(() -> params().getDirection(), v -> params().setDirection(v)),
                new FloatProperty(() -> params().getDirectionVar(), v -> params().setDirectionVar(v))
        );
    }

    private GameObject individualGravityCenter() {
        return new PanelElement.Boolean(
                "Individual Gravity Center",
                new BooleanProperty(() -> params().isIndividualGravityCenter(), v -> params().setIndividualGravityCenter(v))
        );
    }

    private GameObject centripetalAcceleration() {
        return new PanelElement.Float(
                "Centripetal Acceleration",
                new FloatProperty(() -> params().getCentripetalAcceleration(), v -> params().setCentripetalAcceleration(v)),
                new FloatProperty(() -> params().getCentripetalAccelerationVar(), v -> params().setCentripetalAccelerationVar(v))
        );
    }

    private GameObject tangentialAcceleration() {
        return new PanelElement.Float(
                "Tangential Acceleration",
                new FloatProperty(() -> params().getTangentialAcceleration(), v -> params().setTangentialAcceleration(v)),
                new FloatProperty(() -> params().getTangentialAccelerationVar(), v -> params().setTangentialAccelerationVar(v))
        );
    }

    private GameObject accelerationX() {
        return new PanelElement.Float(
                "Acceleration X",
                new FloatProperty(() -> params().getAccelerationX(), v -> params().setAccelerationX(v)),
                new FloatProperty(() -> params().getAccelerationXVar(), v -> params().setAccelerationXVar(v))
        );
    }

    private GameObject accelerationY() {
        return new PanelElement.Float(
                "Acceleration Y",
                new FloatProperty(() -> params().getAccelerationY(), v -> params().setAccelerationY(v)),
                new FloatProperty(() -> params().getAccelerationYVar(), v -> params().setAccelerationYVar(v))
        );
    }

    private GameObject scale() {
        return new PanelElement.Float(
                "Scale",
                new FloatProperty(() -> params().getScale(), v -> params().setScale(v)),
                new FloatProperty(() -> params().getScaleVar(), v -> params().setScaleVar(v))
        );
    }

    private GameObject endScale() {
        PanelElement.Float endScale = new PanelElement.Float(
                "End Scale",
                new FloatProperty(() -> params().getEndScale(), v -> params().setEndScale(v)),
                new FloatProperty(() -> params().getEndScaleVar(), v -> params().setEndScaleVar(v))
        );

        return new PanelElement.Optional(endScale, new BooleanProperty(() -> params().isUpdatingScale(), v -> params().setUpdateScale(v)));
    }

    private GameObject rotatedForward() {
        return new PanelElement.Boolean(
                "Rotated Forward",
                new BooleanProperty(() -> params().isRotatedForward(), v -> params().setRotatedForward(v))
        );
    }

    private GameObject rotation() {
        Panel<GameObject> panel = new DefaultPanel<>();

        PanelElement.Float rotation = new PanelElement.Float(
                "Rotation",
                new FloatProperty(() -> params().getRotation(), v -> params().setRotation(v)),
                new FloatProperty(() -> params().getRotationVar(), v -> params().setRotationVar(v))
        );

        PanelElement.Float rotationSpeed = new PanelElement.Float(
                "Rotation Speed",
                new FloatProperty(() -> params().getRotationSpeed(), v -> params().setRotationSpeed(v)),
                new FloatProperty(() -> params().getRotationSpeedVar(), v -> params().setRotationSpeedVar(v))
        );

        PanelElement.Float rotationAcceleration = new PanelElement.Float(
                "Rotation Acceleration",
                new FloatProperty(() -> params().getRotationAcceleration(), v -> params().setRotationAcceleration(v)),
                new FloatProperty(() -> params().getRotationAccelerationVar(), v -> params().setRotationAccelerationVar(v))
        );

        PanelUtil.stack(panel, rotation, 0f);
        PanelUtil.stack(panel, rotationSpeed);
        PanelUtil.stack(panel, rotationAcceleration);

        return new PanelElement.Optional(panel, new BooleanProperty(() -> !params().isRotatedForward(), v -> params().setRotatedForward(!v)));
    }

    private GameObject colors() {
        Panel<GameObject> panel = new DefaultPanel<>();

        PanelElement.Float r = new PanelElement.Float(
                "Color R",
                new FloatProperty(() -> params().getColorR(), v -> params().setColorR(v)),
                new FloatProperty(() -> params().getColorRVar(), v -> params().setColorRVar(v))
        );

        PanelElement.Float g = new PanelElement.Float(
                "Color G",
                new FloatProperty(() -> params().getColorG(), v -> params().setColorG(v)),
                new FloatProperty(() -> params().getColorGVar(), v -> params().setColorGVar(v))
        );

        PanelElement.Float b = new PanelElement.Float(
                "Color B",
                new FloatProperty(() -> params().getColorB(), v -> params().setColorB(v)),
                new FloatProperty(() -> params().getColorBVar(), v -> params().setColorBVar(v))
        );

        PanelUtil.stack(panel, r, 0f);
        PanelUtil.stack(panel, g);
        PanelUtil.stack(panel, b);

        return panel;
    }

    private GameObject endColors() {
        Panel<GameObject> panel = new DefaultPanel<>();

        PanelElement.Float r = new PanelElement.Float(
                "End Color R",
                new FloatProperty(() -> params().getEndColorR(), v -> params().setEndColorR(v)),
                new FloatProperty(() -> params().getEndColorRVar(), v -> params().setEndColorRVar(v))
        );

        PanelElement.Float g = new PanelElement.Float(
                "End Color G",
                new FloatProperty(() -> params().getEndColorG(), v -> params().setEndColorG(v)),
                new FloatProperty(() -> params().getEndColorGVar(), v -> params().setEndColorGVar(v))
        );

        PanelElement.Float b = new PanelElement.Float(
                "End Color B",
                new FloatProperty(() -> params().getEndColorB(), v -> params().setEndColorB(v)),
                new FloatProperty(() -> params().getEndColorBVar(), v -> params().setEndColorBVar(v))
        );

        PanelUtil.stack(panel, r, 0f);
        PanelUtil.stack(panel, g);
        PanelUtil.stack(panel, b);

        return new PanelElement.Optional(panel, new BooleanProperty(() -> params().isUpdatingColor(), v -> params().setUpdateColor(v)));
    }

    private GameObject alpha() {
        return new PanelElement.Float(
                "Alpha",
                new FloatProperty(() -> params().getAlpha(), v -> params().setAlpha(v)),
                new FloatProperty(() -> params().getAlphaVar(), v -> params().setAlphaVar(v))
        );
    }

    private GameObject endAlpha() {
        PanelElement.Float endAlpha = new PanelElement.Float(
                "End Alpha",
                new FloatProperty(() -> params().getEndAlpha(), v -> params().setEndAlpha(v)),
                new FloatProperty(() -> params().getEndAlphaVar(), v -> params().setEndAlphaVar(v))
        );

        return new PanelElement.Optional(endAlpha, new BooleanProperty(() -> params().isUpdatingAlpha(), v -> params().setUpdateAlpha(v)));
    }
}