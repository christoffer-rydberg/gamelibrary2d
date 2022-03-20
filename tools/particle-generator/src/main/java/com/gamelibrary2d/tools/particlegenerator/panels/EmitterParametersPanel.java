package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.denotations.Parent;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.particles.parameters.EmitterParameters;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.IntegerProperty;

public class EmitterParametersPanel extends AbstractPanel<GameObject> implements Parent<GameObject> {
    private final ParticleSystemModel particleSystem;

    public EmitterParametersPanel(ParticleSystemModel particleSystem) {
        this.particleSystem = particleSystem;
        PanelUtil.stack(this, emissionRate(), 0f);
        PanelUtil.stack(this, particleCount());
    }

    private EmitterParameters settings() {
        return particleSystem.getSettings().getEmitterParameters();
    }

    private GameObject particleCount() {
        return new PanelElement.Integer(
                "Particle Count",
                new IntegerProperty(() -> settings().getParticleCount(), v -> settings().setParticleCount(v)),
                new IntegerProperty(() -> settings().getParticleCountVar(), v -> settings().setParticleCountVar(v))
        );
    }

    private GameObject emissionRate() {
        return new PanelElement.Float(
                "Emission Rate",
                new FloatProperty(() -> settings().getEmissionRate(), v -> settings().setEmissionRate(v))
        );
    }
}