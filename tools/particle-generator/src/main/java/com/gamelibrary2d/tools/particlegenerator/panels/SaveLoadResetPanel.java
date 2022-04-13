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
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.particles.ParticleEmissionParameters;
import com.gamelibrary2d.particles.ParticleSpawnParameters;
import com.gamelibrary2d.particles.ParticleSystemParameters;
import com.gamelibrary2d.particles.ParticleUpdateParameters;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
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
                        new ParticleEmissionParameters(),
                        new ParticleSpawnParameters(),
                        new ParticleUpdateParameters());
            }
        } catch (IOException e) {
            settings = new ParticleSystemParameters(
                    new ParticleEmissionParameters(),
                    new ParticleSpawnParameters(),
                    new ParticleUpdateParameters());
        }

        particleSystem.setSettings(settings);
    }

    private void randomizeParticleSystem() {
        RandomGenerator random = RandomInstance.get();

        ParticleUpdateParameters updateParameters = particleSystem.getParameters().getUpdateParameters();
        ParticleSpawnParameters spawnParameters = particleSystem.getParameters().getSpawnParameters();

        float life = round(random.nextFloat() * 5, 1);
        updateParameters.setLife(life);
        updateParameters.setLifeVar(round(random.nextFloat() * life, 1));

        updateParameters.setMovingFromCenter(random.nextInt(2) == 1);
        updateParameters.setSpeed(random.nextInt(10));
        updateParameters.setSpeedVar(random.nextInt(10));

        updateParameters.setDirection(round(random.nextFloat() * 360 - 180, 1));
        updateParameters.setDirectionVar(round(random.nextFloat() * 180, 1));

        boolean downScale = random.nextInt(2) == 1;
        float scale = random.nextFloat() * 5;
        updateParameters.setScale(round(downScale ? 1f / scale : scale, 2));

        boolean downScaleVar = random.nextInt(2) == 1;
        float scaleVar = random.nextFloat() * 2;
        updateParameters.setScaleVar(round(downScaleVar ? 1f / scaleVar : scaleVar, 2));

        updateParameters.setUpdateScale(random.nextInt(2) == 1);

        boolean downScaleEnd = random.nextInt(2) == 1;
        float scaleEnd = random.nextFloat() * 5;
        updateParameters.setEndScale(round(downScaleEnd ? 1f / scaleEnd : scaleEnd, 2));

        boolean downScaleEndVar = random.nextInt(2) == 1;
        float endScaleVar = random.nextFloat() * 2;
        updateParameters.setEndScaleVar(round(downScaleEndVar ? 1f / endScaleVar : endScaleVar, 2));

        updateParameters.setAcceleration(
                round(random.nextFloat() * 100 - 50, 1),
                round(random.nextFloat() * 100 - 50, 1));

        updateParameters.setAccelerationVar(
                round(random.nextFloat() * 100 - 50, 1),
                round(random.nextFloat() * 100 - 50, 1));

        updateParameters.setCentripetalAcceleration(round(random.nextFloat() * 100, 1));
        updateParameters.setCentripetalAccelerationVar(round(random.nextFloat() * 100, 1));

        updateParameters.setTangentialAcceleration(round(random.nextFloat() * 100, 1));
        updateParameters.setTangentialAccelerationVar(round(random.nextFloat() * 100, 1));

        updateParameters.setColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        updateParameters.setUpdateColor(random.nextInt(2) == 1);

        updateParameters.setEndColor(random.nextInt(255), random.nextInt(255), random.nextInt(255));

        boolean varColor = random.nextInt(2) == 1;
        updateParameters.setColorVar(varColor ? random.nextInt(255) : 0, varColor ? random.nextInt(255) : 0,
                varColor ? random.nextInt(255) : 0);

        boolean varEndColor = random.nextInt(2) == 1;
        updateParameters.setEndColorVar(varEndColor ? random.nextInt(255) : 0, varEndColor ? random.nextInt(255) : 0,
                varEndColor ? random.nextInt(255) : 0);

        boolean varEndSpeed = random.nextInt(2) == 1;
        updateParameters.setEndSpeedFactor(varEndSpeed ? round(random.nextFloat(), 2) : 1);

        updateParameters.setAlpha(round(random.nextFloat(), 2));
        updateParameters.setAlphaVar(round(random.nextFloat(), 2));
        updateParameters.setEndAlpha(round(random.nextFloat(), 2));
        updateParameters.setEndAlphaVar(round(random.nextFloat(), 2));
        updateParameters.setUpdateAlpha(random.nextInt(2) == 1);

        boolean randomizeRotation = random.nextInt(2) == 1;
        if (randomizeRotation) {
            boolean rotatedForward = random.nextInt(2) == 1;
            if (rotatedForward) {
                updateParameters.setRotatedForward(true);
                updateParameters.setRotation(0);
                updateParameters.setRotationVar(0);
                updateParameters.setRotationSpeed(0);
                updateParameters.setRotationSpeedVar(0);
                updateParameters.setRotationAcceleration(0);
                updateParameters.setRotationAccelerationVar(0);
            } else {
                updateParameters.setRotatedForward(false);
                updateParameters.setRotation(round(random.nextFloat() * 360 - 180, 2));
                updateParameters.setRotationVar(round(random.nextFloat() * 180, 2));
                updateParameters.setRotationSpeed(round(random.nextFloat() * 100, 2));
                updateParameters.setRotationSpeedVar(round(random.nextFloat() * 100, 2));
                updateParameters.setRotationAcceleration(round(random.nextFloat() * 100, 2));
                updateParameters.setRotationAccelerationVar(round(random.nextFloat() * 100, 2));
            }
        } else {
            updateParameters.setRotation(0);
            updateParameters.setRotationVar(0);
            updateParameters.setRotationSpeed(0);
            updateParameters.setRotationSpeedVar(0);
            updateParameters.setRotationAcceleration(0);
            updateParameters.setRotationAccelerationVar(0);
        }

        updateParameters.setIndividualGravityCenter(random.nextInt(2) == 1);

        ParticleEmissionParameters emissionParameters = particleSystem.getSettings().getEmissionParameters();
        emissionParameters.setEmissionRate(round(random.nextFloat() * 750, 1));
        emissionParameters.setParticleCount(1);
        emissionParameters.setParticleCountVar(1);

        final float windowWidth = game.getWindow().getWidth();
        final float windowHeight = game.getWindow().getHeight();

        spawnParameters.setOffsetX(0f);
        spawnParameters.setOffsetY(0f);

        spawnParameters.setOffsetXVar(
                round(random.nextFloat() * windowWidth / 8f, 0));

        spawnParameters.setOffsetYVar(
                round(random.nextFloat() * windowHeight / 8f, 0));

        spawnParameters.setRadiusX(
                round(random.nextFloat() * windowWidth / 8f, 0));

        spawnParameters.setRadiusXVar(
                round(random.nextFloat() * windowHeight / 8f, 0));

        spawnParameters.setRadiusY(
                round(random.nextFloat() * windowWidth / 8f, 0));

        spawnParameters.setRadiusYVar(
                round(random.nextFloat() * windowHeight / 8f, 0));

        spawnParameters
                .setAngle(round(random.nextFloat() * 360 - 180, 1));
        spawnParameters
                .setAngleVar(round(random.nextFloat() * 180, 1));
    }

    private float round(float value, int decimals) {
        int tmp = (int) Math.pow(10, decimals);
        return (int) (value * tmp) / (float) tmp;
    }
}