package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.denotations.Parent;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.particles.parameters.PositionParameters;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;

public class PositionParametersPanel extends AbstractPanel<GameObject> implements Parent<GameObject> {

    private final ParticleSystemModel particleSystem;

    public PositionParametersPanel(ParticleSystemModel particleSystem) {
        this.particleSystem = particleSystem;
        PanelUtil.stack(this, angle());
        PanelUtil.stack(this, radiusX());
        PanelUtil.stack(this, radiusY());
        PanelUtil.stack(this, varX());
        PanelUtil.stack(this, varY());
    }

    private PositionParameters params() {
        return particleSystem.getSettings().getPositionParameters();
    }

    private GameObject angle() {
        return new PanelElement.Float(
                "Spawn Angle",
                new FloatProperty(() -> params().getAngle(), v -> params().setAngle(v)),
                new FloatProperty(() -> params().getAngleVar(), v -> params().setAngleVar(v))
        );
    }

    private GameObject radiusX() {
        return new PanelElement.Float(
                "Spawn Radius X",
                new FloatProperty(() -> params().getRadiusX(), v -> params().setRadiusX(v)),
                new FloatProperty(() -> params().getRadiusXVar(), v -> params().setRadiusXVar(v))
        );
    }

    private GameObject radiusY() {
        return new PanelElement.Float(
                "Spawn Radius Y",
                new FloatProperty(() -> params().getRadiusY(), v -> params().setRadiusY(v)),
                new FloatProperty(() -> params().getRadiusYVar(), v -> params().setRadiusYVar(v))
        );
    }

    private GameObject varX() {
        return new PanelElement.Float(
                "Spawn Offset X",
                new FloatProperty(() -> params().getOffsetX(), v -> params().setOffsetX(v)),
                new FloatProperty(() -> params().getOffsetXVar(), v -> params().setOffsetXVar(v))
        );
    }

    private GameObject varY() {
        return new PanelElement.Float(
                "Spawn Offset Y",
                new FloatProperty(() -> params().getOffsetY(), v -> params().setOffsetY(v)),
                new FloatProperty(() -> params().getOffsetYVar(), v -> params().setOffsetYVar(v))
        );
    }
}