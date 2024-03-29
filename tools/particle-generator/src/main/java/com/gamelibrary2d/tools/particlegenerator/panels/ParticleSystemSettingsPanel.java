package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.denotations.Container;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.functional.Func;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.io.Serializable;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.particles.ParticleSpawnParameters;
import com.gamelibrary2d.particles.ParticleUpdateParameters;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.resources.Textures;
import com.gamelibrary2d.tools.particlegenerator.widgets.Slider;

public class ParticleSystemSettingsPanel extends AbstractPanel<GameObject> implements Container<GameObject> {

    private static final DataBuffer ioBuffer = new DynamicByteBuffer();

    public ParticleSystemSettingsPanel(ParticleSystemModel particleSystem, Disposer disposer) {
        PanelUtil.stack(this, new UpdateParametersPanel(particleSystem), 0f);
        PanelUtil.stack(this, new SpawnParametersPanel(particleSystem), PanelUtil.DEFAULT_STACK_MARGIN * 5);
        PanelUtil.stack(this, new EmissionParametersPanel(particleSystem), PanelUtil.DEFAULT_STACK_MARGIN * 5);

        ResizeSlider slider = ResizeSlider.create(particleSystem, disposer);
        PanelUtil.stack(this, slider, PanelUtil.DEFAULT_STACK_MARGIN * 5);
    }

    private static <T extends Serializable> T createCopy(T target, Func<DataBuffer, T> factory) {
        ioBuffer.clear();
        target.serialize(ioBuffer);
        ioBuffer.flip();
        return factory.invoke(ioBuffer);
    }

    private static class ResizeSlider extends Slider {
        private ParticleSpawnParameters originalParticleSpawnParameters;
        private ParticleUpdateParameters originalParticleUpdateParameters;

        private ResizeSlider(ContentRenderer handle, ParticleSystemModel particleSystem) {
            super(handle, SliderDirection.HORIZONTAL, -50, 50, 2);
            addDragBeginListener(value -> {
                originalParticleSpawnParameters = createCopy(particleSystem.getSpawnParameters(), ParticleSpawnParameters::new);
                originalParticleUpdateParameters = createCopy(particleSystem.getParameters().getUpdateParameters(), ParticleUpdateParameters::new);
            });
            addValueChangedListener(value -> {
                float resizeValue = ((value < 0 ? value : value * 2) + 100f) * 0.01f;
                ParticleSpawnParameters updatedSpawnParameters
                        = createCopy(originalParticleSpawnParameters, ParticleSpawnParameters::new);
                ParticleUpdateParameters updatedUpdateParameters =
                        createCopy(originalParticleUpdateParameters, ParticleUpdateParameters::new);

                updatedSpawnParameters.scale(resizeValue);
                updatedUpdateParameters.scale(resizeValue);

                particleSystem.setSpawnParameters(updatedSpawnParameters);
                particleSystem.setParameters(updatedUpdateParameters);
            });
            addDragStopListener(v -> setValue(0, false));

            setBounds(Rectangle.create(handle.getBounds().getWidth() + 200, handle.getBounds().getHeight()));
        }

        static ResizeSlider create(ParticleSystemModel particleSystem, Disposer disposer) {
            Surface quad = Quad.create(Rectangle.create(32, 16), disposer);
            ContentRenderer handle = new SurfaceRenderer<>(quad, Textures.sliderHandle());
            return new ResizeSlider(handle, particleSystem);
        }
    }
}