package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.common.io.ResourceReader;
import com.gamelibrary2d.common.io.ResourceWriter;
import com.gamelibrary2d.common.random.RandomGenerator;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.renderers.Label;
import com.gamelibrary2d.particles.parameters.EmitterParameters;
import com.gamelibrary2d.particles.parameters.ParticleParameters;
import com.gamelibrary2d.particles.parameters.ParticleSystemParameters;
import com.gamelibrary2d.particles.parameters.PositionParameters;
import com.gamelibrary2d.resources.HorizontalTextAlignment;
import com.gamelibrary2d.resources.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.widgets.Button;

import java.io.File;
import java.io.IOException;

public class SaveLoadResetPanel extends AbstractPanel<GameObject> {
    private final ParticleSystemModel particleSystem;

    private final DataBuffer ioBuffer = new DynamicByteBuffer();
    private final ResourceReader resourceReader = new ResourceReader(ioBuffer);
    private final ResourceWriter resourceWriter = new ResourceWriter(ioBuffer);

    private final Game game;
    private final FileChooser fileChooser;

    public SaveLoadResetPanel(ParticleSystemModel particleSystem, Game game) {
        this.particleSystem = particleSystem;
        this.game = game;

        fileChooser = new FileChooser(System.getenv("TEMP") + "/ParticleGenerator/particle_path.txt");

        addButton("Save", 0, 0, this::saveParticleSystem);
        addButton("Load", 100, 0, this::loadParticleSystem);
        addButton("Reset", 200, 0, particleSystem::reset);
        addButton("Randomize", 300, 0, this::randomizeParticleSystem);
    }

    private void addButton(String text, float posX, float posY, Action onClick) {
        Label label = new Label(Fonts.getDefaultFont(), text);
        label.setColor(Color.SOFT_BLUE);
        label.setAlignment(HorizontalTextAlignment.LEFT, VerticalTextAlignment.BASE_LINE);

        Button button = new Button(label, onClick);
        button.setPosition(posX, posY);
        button.setBounds(label.calculateBounds());
        add(button);
    }

    private void saveParticleSystem() {
        try {
            File file = fileChooser.browse(FileSelectionMode.FILES_ONLY);
            if (file != null) {
                resourceWriter.write(particleSystem.getSettings(), file, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadParticleSystem() {
        ParticleSystemParameters settings;
        try {
            File file = fileChooser.browse(FileSelectionMode.FILES_ONLY);

            if (file != null) {
                settings = resourceReader.read(file, ParticleSystemParameters::new);
            } else {
                settings = new ParticleSystemParameters(
                        new EmitterParameters(),
                        new PositionParameters(),
                        new ParticleParameters());
            }
        } catch (IOException e) {
            settings = new ParticleSystemParameters(
                    new EmitterParameters(),
                    new PositionParameters(),
                    new ParticleParameters());
        }

        particleSystem.setSettings(settings);
    }

    private void randomizeParticleSystem() {
        RandomGenerator random = RandomInstance.get();

        ParticleParameters particleParameters = particleSystem.getParameters().getParticleParameters();

        float life = round(random.nextFloat() * 5, 1);
        particleParameters.setLife(life);
        particleParameters.setLifeVar(round(random.nextFloat() * life, 1));

        particleParameters.setMoveFromCenter(random.nextInt(2) == 1);
        particleParameters.setSpeed(random.nextInt(10));
        particleParameters.setSpeedVar(random.nextInt(10));

        particleParameters.setDirection(round(random.nextFloat() * 360 - 180, 1));
        particleParameters.setDirectionVar(round(random.nextFloat() * 180, 1));

        boolean downScale = random.nextInt(2) == 1;
        float scale = random.nextFloat() * 5;
        particleParameters.setScale(round(downScale ? 1f / scale : scale, 2));

        boolean downScaleVar = random.nextInt(2) == 1;
        float scaleVar = random.nextFloat() * 2;
        particleParameters.setScaleVar(round(downScaleVar ? 1f / scaleVar : scaleVar, 2));

        particleParameters.setUpdateScale(random.nextInt(2) == 1);

        boolean downScaleEnd = random.nextInt(2) == 1;
        float scaleEnd = random.nextFloat() * 5;
        particleParameters.setEndScale(round(downScaleEnd ? 1f / scaleEnd : scaleEnd, 2));

        boolean downScaleEndVar = random.nextInt(2) == 1;
        float endScaleVar = random.nextFloat() * 2;
        particleParameters.setEndScaleVar(round(downScaleEndVar ? 1f / endScaleVar : endScaleVar, 2));

        particleParameters.setAcceleration(
                round(random.nextFloat() * 100 - 50, 1),
                round(random.nextFloat() * 100 - 50, 1));

        particleParameters.setAccelerationVar(
                round(random.nextFloat() * 100 - 50, 1),
                round(random.nextFloat() * 100 - 50, 1));

        particleParameters.setCentripetalAcceleration(round(random.nextFloat() * 100, 1));
        particleParameters.setCentripetalAccelerationVar(round(random.nextFloat() * 100, 1));

        particleParameters.setTangentalAcceleration(round(random.nextFloat() * 100, 1));
        particleParameters.setTangentalAccelerationVar(round(random.nextFloat() * 100, 1));

        particleParameters.setColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        particleParameters.setUpdateColor(random.nextInt(2) == 1);

        particleParameters.setEndColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));

        boolean varColor = random.nextInt(2) == 1;
        particleParameters.setColorVar(varColor ? random.nextInt(255) : 0, varColor ? random.nextInt(255) : 0,
                varColor ? random.nextInt(255) : 0);

        boolean varEndColor = random.nextInt(2) == 1;
        particleParameters.setEndColorVar(varEndColor ? random.nextInt(255) : 0, varEndColor ? random.nextInt(255) : 0,
                varEndColor ? random.nextInt(255) : 0);

        boolean varEndSpeed = random.nextInt(2) == 1;
        particleParameters.setEndSpeedFactor(varEndSpeed ? round(random.nextFloat(), 2) : 1);

        particleParameters.setAlpha(round(random.nextFloat(), 2));
        particleParameters.setAlphaVar(round(random.nextFloat(), 2));
        particleParameters.setEndAlpha(round(random.nextFloat(), 2));
        particleParameters.setEndAlphaVar(round(random.nextFloat(), 2));
        particleParameters.setUpdateAlpha(random.nextInt(2) == 1);

        boolean randomizeRotation = random.nextInt(2) == 1;
        if (randomizeRotation) {
            boolean rotatedForward = random.nextInt(2) == 1;
            if (rotatedForward) {
                particleParameters.setRotatedForward(true);
                particleParameters.setRotation(0);
                particleParameters.setRotationVar(0);
                particleParameters.setRotationSpeed(0);
                particleParameters.setRotationSpeedVar(0);
                particleParameters.setRotationAcceleration(0);
                particleParameters.setRotationAccelerationVar(0);
            } else {
                particleParameters.setRotatedForward(false);
                particleParameters.setRotation(round(random.nextFloat() * 360 - 180, 2));
                particleParameters.setRotationVar(round(random.nextFloat() * 180, 2));
                particleParameters.setRotationSpeed(round(random.nextFloat() * 100, 2));
                particleParameters.setRotationSpeedVar(round(random.nextFloat() * 100, 2));
                particleParameters.setRotationAcceleration(round(random.nextFloat() * 100, 2));
                particleParameters.setRotationAccelerationVar(round(random.nextFloat() * 100, 2));
            }
        } else {
            particleParameters.setRotation(0);
            particleParameters.setRotationVar(0);
            particleParameters.setRotationSpeed(0);
            particleParameters.setRotationSpeedVar(0);
            particleParameters.setRotationAcceleration(0);
            particleParameters.setRotationAccelerationVar(0);
        }

        int origin = random.nextInt(PositionParameters.SpawnArea.values().length);
        PositionParameters.SpawnArea spawnArea = PositionParameters.SpawnArea.values()[origin];
        particleSystem.getPositioner().setSpawnArea(spawnArea);

        EmitterParameters emitterParameters = particleSystem.getSettings().getEmitterParameters();
        emitterParameters.setEmissionRate(round(random.nextFloat() * 750, 1));
        emitterParameters.setParticleCount(1);
        emitterParameters.setParticleCountVar(1);

        boolean localCenter = random.nextInt(2) == 1;
        particleSystem.getPositioner().setLocalCenter(localCenter);

        final float windowWidth = game.getWindow().getWidth();
        final float windowHeight = game.getWindow().getHeight();

        int areaRandomizer = random.nextInt(3);
        if (areaRandomizer == 0) {
            particleSystem.getPositioner().setSpawnAreaWidth(0);
            particleSystem.getPositioner().setSpawnAreaHeight(0);

            particleSystem.getPositioner().setSpawnAreaWidthVar(
                    round(random.nextFloat() * windowWidth / 4f, 0));

            particleSystem.getPositioner().setSpawnAreaHeightVar(
                    round(random.nextFloat() * windowHeight / 4f, 0));
        } else if (areaRandomizer == 1) {
            particleSystem.getPositioner().setSpawnAreaWidth(
                    round(random.nextFloat() * windowWidth / 4f, 0));

            particleSystem.getPositioner().setSpawnAreaHeight(
                    round(random.nextFloat() * windowHeight / 4f, 0));

            particleSystem.getPositioner().setSpawnAreaWidthVar(0);
            particleSystem.getPositioner().setSpawnAreaHeightVar(0);
        } else if (areaRandomizer == 2) {
            particleSystem.getPositioner().setSpawnAreaWidthVar(
                    round(random.nextFloat() * windowWidth / 8f, 0));

            particleSystem.getPositioner().setSpawnAreaHeightVar(
                    round(random.nextFloat() * windowHeight / 8f, 0));

            particleSystem.getPositioner().setSpawnAreaWidth(
                    round(random.nextFloat() * windowWidth / 8f, 0));

            particleSystem.getPositioner().setSpawnAreaHeight(
                    round(random.nextFloat() * windowHeight / 8f, 0));
        }

        particleSystem.getPositioner()
                .setSpawnAngle(round(random.nextFloat() * 360 - 180, 1));
        particleSystem.getPositioner()
                .setSpawnAngleVar(round(random.nextFloat() * 180, 1));
    }

    private float round(float value, int decimals) {
        int tmp = (int) Math.pow(10, decimals);
        return (int) (value * tmp) / (float) tmp;
    }
}