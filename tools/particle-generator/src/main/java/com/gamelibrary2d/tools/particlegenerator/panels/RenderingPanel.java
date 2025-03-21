package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.containers.DefaultPanel;
import com.gamelibrary2d.components.containers.Panel;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.renderers.PointSmoothing;
import com.gamelibrary2d.particles.ParticleShape;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.GenericProperty;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.widgets.Button;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class RenderingPanel extends AbstractPanel<GameObject> {

    private final static String TEMP_PATH = System.getenv("TEMP") + "/ParticleGenerator/texture_path.txt";

    private final FileChooser fileChooser;
    private final ParticleSystemModel particleSystem;
    private final Panel<GameObject> sequentialPanel;
    private final Panel<GameObject> efficientPanel;
    private final Panel<GameObject> acceleratedPanel;
    private final ShapePanel shapePanel;
    private final Label textureLabel;

    private Panel<GameObject> activePanel;

    public RenderingPanel(ParticleSystemModel particleSystem) {
        fileChooser = new FileChooser(TEMP_PATH);

        textureLabel = new Label(Fonts.getDefaultFont(), "None");
        textureLabel.setColor(Color.LIGHT_BLUE);
        textureLabel.setAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.BASE_LINE);

        this.particleSystem = particleSystem;
        this.shapePanel = new ShapePanel(particleSystem);
        this.sequentialPanel = createSequentialPanel();
        this.efficientPanel = createEfficientPanel();
        this.acceleratedPanel = createAcceleratedPanel();

        PanelUtil.stack(this, particleSystemType(), 0f);
        PanelUtil.stack(this, blendMode());
        setActivePanel(efficientPanel);
    }

    private void updateActivePanel() {
        switch (particleSystem.getParticleSystemType()) {
            case SEQUENTIAL:
                setActivePanel(sequentialPanel);
                break;
            case EFFICIENT:
                setActivePanel(efficientPanel);
                break;
            case ACCELERATED:
                setActivePanel(acceleratedPanel);
                break;
        }
    }

    private String loadTexture() {
        try {
            File file = fileChooser.browse(FileSelectionMode.FILES_ONLY);
            if (file != null) {
                URL url = file.toURI().toURL();
                particleSystem.loadTexture(url);
                return file.getName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        particleSystem.removeTexture();
        return null;
    }

    @Override
    public void onRender(float alpha) {
        updateActivePanel();
        shapePanel.updateActivePanel();
        super.onRender(alpha);
    }

    private void setActivePanel(Panel<GameObject> activePanel) {
        if (this.activePanel != activePanel) {
            remove(this.activePanel);
            recalculateBounds();
            this.activePanel = activePanel;
            PanelUtil.stack(this, activePanel);
        }
    }

    private GameObject particleSystemType() {
        return new PanelElement.Enum<>(
                "Particle System",
                ParticleSystemModel.ParticleSystemType.class,
                new GenericProperty<>(particleSystem::getParticleSystemType, particleSystem::setParticleSystemType)
        );
    }

    private GameObject blendMode() {
        return new PanelElement.Enum<>(
                "Blend Mode",
                BlendMode.class,
                new GenericProperty<>(particleSystem::getBlendMode, particleSystem::setBlendMode)
        );
    }

    private Panel<GameObject> createSequentialPanel() {
        Panel<GameObject> panel = new DefaultPanel<>();
        PanelUtil.stack(panel, createQuadPanel(), 0f);
        return panel;
    }

    private Panel<GameObject> createEfficientPanel() {
        Panel<GameObject> panel = new DefaultPanel<>();
        PanelUtil.stack(panel, shapePanel, 0f);
        return panel;
    }

    private Panel<GameObject> createAcceleratedPanel() {
        Panel<GameObject> panel = new DefaultPanel<>();
        PanelUtil.stack(panel, shapePanel, 0f);
        return panel;
    }

    private Panel<GameObject> createQuadPanel() {
        Panel<GameObject> panel = new DefaultPanel<>();

        PanelElement.Float widthElement = new PanelElement.Float(
                "Width",
                new FloatProperty(
                        particleSystem::getWidth,
                        particleSystem::setWidth)
        );

        PanelElement.Float heightElement = new PanelElement.Float(
                "Height",
                new FloatProperty(
                        particleSystem::getHeight,
                        particleSystem::setHeight)
        );

        Button textureButton = new Button(textureLabel, () -> {
            String fileName = loadTexture();
            textureLabel.setText(fileName == null ? "None" : fileName);
        });

        textureButton.setBounds(textureLabel.getBounds());

        PanelElement.CustomElement textureElement = new PanelElement.CustomElement("Texture", textureButton);

        PanelUtil.stack(panel, widthElement, 0f);
        PanelUtil.stack(panel, heightElement);
        PanelUtil.stack(panel, textureElement);
        return panel;
    }

    private class ShapePanel extends AbstractPanel<GameObject> {
        private final ParticleSystemModel particleSystem;
        private final Panel<GameObject> pointPanel;
        private final Panel<GameObject> quadPanel;

        private Panel<GameObject> activePanel;

        ShapePanel(ParticleSystemModel particleSystem) {
            this.particleSystem = particleSystem;
            this.quadPanel = createQuadPanel();
            this.pointPanel = createPointShapePanel();
            PanelUtil.stack(this, particleShape(), 0f);
            updateActivePanel();
        }

        void updateActivePanel() {
            switch (particleSystem.getEfficientRenderer().getParticleShape()) {
                case POINT:
                    setActivePanel(pointPanel);
                    break;
                case QUAD:
                    setActivePanel(quadPanel);
                    break;
            }
        }

        private void setActivePanel(Panel<GameObject> activePanel) {
            if (this.activePanel != activePanel) {
                remove(this.activePanel);
                recalculateBounds();
                this.activePanel = activePanel;
                PanelUtil.stack(this, activePanel);
            }
        }

        private GameObject particleShape() {
            return new PanelElement.Enum<>(
                    "Particle Shape",
                    ParticleShape.class,
                    new GenericProperty<>(
                            () -> particleSystem.getEfficientRenderer().getParticleShape(),
                            v -> particleSystem.getEfficientRenderer().setParticleShape(v))
            );
        }

        private GameObject pointSize() {
            return new PanelElement.Float(
                    "Point Size",
                    new FloatProperty(
                            () -> particleSystem.getEfficientRenderer().getPointSize(),
                            v -> particleSystem.getEfficientRenderer().setPointSize(v))
            );
        }

        private GameObject pointSmoothing() {
            return new PanelElement.Enum<>(
                    "Point Smoothing",
                    PointSmoothing.class,
                    new GenericProperty<>(
                            () -> particleSystem.getEfficientRenderer().getPointSmoothing(),
                            v -> particleSystem.getEfficientRenderer().setPointSmoothing(v))
            );
        }

        private Panel<GameObject> createPointShapePanel() {
            Panel<GameObject> panel = new DefaultPanel<>();
            PanelUtil.stack(panel, pointSize(), 0f);
            PanelUtil.stack(panel, pointSmoothing());
            return panel;
        }
    }
}