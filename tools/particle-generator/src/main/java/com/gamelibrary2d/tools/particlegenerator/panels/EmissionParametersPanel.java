package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.denotations.Parent;
import com.gamelibrary2d.particles.ParticleEmissionParameters;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.IntegerProperty;

public class EmissionParametersPanel extends AbstractPanel<GameObject> implements Parent<GameObject> {
    private final ParticleSystemModel particleSystem;

    public EmissionParametersPanel(ParticleSystemModel particleSystem) {
        this.particleSystem = particleSystem;
        PanelUtil.stack(this, emissionRate(), 0f);
        PanelUtil.stack(this, particleCount());
    }

    private ParticleEmissionParameters settings() {
        return particleSystem.getSettings().getEmissionParameters();
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