package com.gamelibrary2d.particle;

import com.gamelibrary2d.particle.systems.DefaultParticleSystem;
import com.gamelibrary2d.particle.systems.Particle;

public interface ParticleUpdateListener {

    /**
     * @param system   The particle system owning the particle.
     * @param particle The updated particle.
     * @return True if the particle has collided and should be destroyed.
     */
    boolean updated(DefaultParticleSystem system, Particle particle);

}