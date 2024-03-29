package com.gamelibrary2d.particles;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Serializable;
import com.gamelibrary2d.random.RandomInstance;

public class ParticleUpdateParameters implements Serializable {
    private final static int STRIDE = 52;

    private final static int LIFE = 0;
    private final static int LIFE_VAR = 1;
    private final static int DELAY = 2;
    private final static int DELAY_VAR = 3;

    private final static int SPEED = 4;
    private final static int SPEED_VAR = 5;
    private final static int END_SPEED_FACTOR = 6;
    private final static int END_SPEED_FACTOR_VAR = 7;

    private final static int SCALE = 8;
    private final static int SCALE_VAR = 9;
    private final static int END_SCALE = 10;
    private final static int END_SCALE_VAR = 11;

    private final static int UPDATE_SCALE = 12;
    private final static int DIRECTION = 13;
    private final static int DIRECTION_VAR = 14;
    private final static int MOVE_FROM_CENTER = 15;

    private final static int ACCELERATION_X = 16;
    private final static int ACCELERATION_X_VAR = 17;
    private final static int ACCELERATION_Y = 18;
    private final static int ACCELERATION_Y_VAR = 19;

    private final static int CENTRIPETAL_ACCELERATION = 20;
    private final static int CENTRIPETAL_ACCELERATION_VAR = 21;
    private final static int TANGENTIAL_ACCELERATION = 22;
    private final static int TANGENTIAL_ACCELERATION_VAR = 23;

    private final static int ROTATION = 24;
    private final static int ROTATION_VAR = 25;
    private final static int ROTATION_SPEED = 26;
    private final static int ROTATION_SPEED_VAR = 27;

    private final static int ROTATION_ACCELERATION = 28;
    private final static int ROTATION_ACCELERATION_VAR = 29;
    private final static int ROTATED_FORWARD = 30;
    private final static int UPDATE_COLOR = 31;

    private final static int COLOR_R = 32;
    private final static int COLOR_R_VAR = 33;
    private final static int COLOR_G = 34;
    private final static int COLOR_G_VAR = 35;

    private final static int COLOR_B = 36;
    private final static int COLOR_B_VAR = 37;
    private final static int END_COLOR_R = 38;
    private final static int END_COLOR_R_VAR = 39;

    private final static int END_COLOR_G = 40;
    private final static int END_COLOR_G_VAR = 41;
    private final static int END_COLOR_B = 42;
    private final static int END_COLOR_B_VAR = 43;

    private final static int ALPHA = 44;
    private final static int ALPHA_VAR = 45;
    private final static int END_ALPHA = 46;
    private final static int END_ALPHA_VAR = 47;

    private final static int UPDATE_ALPHA = 48;
    private final static int INDIVIDUAL_GRAVITY_CENTER = 49;

    private final float[] internalState = new float[STRIDE];
    private int updateCounter;

    /**
     * Default constructor
     */
    public ParticleUpdateParameters() {
        internalState[LIFE] = 1;
        internalState[COLOR_R] = 255;
        internalState[COLOR_G] = 255;
        internalState[COLOR_B] = 255;
        internalState[ALPHA] = 1;
        internalState[SCALE] = 1;
        internalState[END_SPEED_FACTOR] = 1;
    }

    /**
     * Copies settings from the serialized buffer.
     */
    public ParticleUpdateParameters(DataBuffer buffer) {
        for (int i = 0; i < STRIDE; ++i) {
            internalState[i] = buffer.getFloat();
        }
    }

    @Override
    public void serialize(DataBuffer buffer) {
        for (int i = 0; i < STRIDE; ++i) {
            buffer.putFloat(internalState[i]);
        }
    }

    public float[] getInternalStateArray() {
        return internalState;
    }

    public void setUpdateScale(boolean updateScale) {
        setInternalState(UPDATE_SCALE, updateScale ? 1f : 0f);
    }

    public boolean isIndividualGravityCenter() {
        return internalState[INDIVIDUAL_GRAVITY_CENTER] != 0;
    }

    public void setIndividualGravityCenter(boolean individualGravityCenter) {
        setInternalState(INDIVIDUAL_GRAVITY_CENTER, individualGravityCenter ? 1 : 0);
    }

    public void setAcceleration(float x, float y) {
        setInternalState(ACCELERATION_X, x);
        setInternalState(ACCELERATION_Y, y);
    }

    public void setAccelerationVar(float xVar, float yVar) {
        setInternalState(ACCELERATION_X_VAR, xVar);
        setInternalState(ACCELERATION_Y_VAR, yVar);
    }

    public void setUpdateColor(boolean updateColor) {
        setInternalState(UPDATE_COLOR, updateColor ? 1f : 0f);
    }

    public void setColor(float r, float g, float b) {
        setInternalState(COLOR_R, r);
        setInternalState(COLOR_G, g);
        setInternalState(COLOR_B, b);
    }

    public void setColorVar(float r, float g, float b) {
        setInternalState(COLOR_R_VAR, r);
        setInternalState(COLOR_G_VAR, g);
        setInternalState(COLOR_B_VAR, b);
    }

    public void setEndColor(float r, float g, float b) {
        setInternalState(END_COLOR_R, r);
        setInternalState(END_COLOR_G, g);
        setInternalState(END_COLOR_B, b);
    }

    public void setEndColorVar(float r, float g, float b) {
        setInternalState(END_COLOR_R_VAR, r);
        setInternalState(END_COLOR_G_VAR, g);
        setInternalState(END_COLOR_B_VAR, b);
    }

    public boolean isRotatedForward() {
        return internalState[ROTATED_FORWARD] == 1f;
    }

    public void setRotatedForward(boolean rotatedForward) {
        setInternalState(ROTATED_FORWARD, rotatedForward ? 1f : 0f);
    }

    public float getDelay() {
        return internalState[DELAY];
    }

    public void setDelay(float delay) {
        setInternalState(DELAY, delay);
    }

    public float getDelayVar() {
        return internalState[DELAY_VAR];
    }

    public void setDelayVar(float delayVar) {
        setInternalState(DELAY_VAR, delayVar);
    }

    public float getLife() {
        return internalState[LIFE];
    }

    public void setLife(float life) {
        setInternalState(LIFE, life);
    }

    public float getLifeVar() {
        return internalState[LIFE_VAR];
    }

    public void setLifeVar(float lifeVar) {
        setInternalState(LIFE_VAR, lifeVar);
    }

    public float getSpeed() {
        return internalState[SPEED];
    }

    public void setSpeed(float speed) {
        setInternalState(SPEED, speed);
    }

    public float getSpeedVar() {
        return internalState[SPEED_VAR];
    }

    public void setSpeedVar(float speedVar) {
        setInternalState(SPEED_VAR, speedVar);
    }

    public float getEndSpeedFactor() {
        return internalState[END_SPEED_FACTOR];
    }

    public void setEndSpeedFactor(float endSpeedFactor) {
        setInternalState(END_SPEED_FACTOR, endSpeedFactor);
    }

    public float getEndSpeedFactorVar() {
        return internalState[END_SPEED_FACTOR_VAR];
    }

    public void setEndSpeedFactorVar(float endSpeedFactorVar) {
        setInternalState(END_SPEED_FACTOR_VAR, endSpeedFactorVar);
    }

    public float getDirection() {
        return internalState[DIRECTION];
    }

    public void setDirection(float direction) {
        setInternalState(DIRECTION, direction);
    }

    public float getDirectionVar() {
        return internalState[DIRECTION_VAR];
    }

    public void setDirectionVar(float directionVar) {
        setInternalState(DIRECTION_VAR, directionVar);
    }

    public boolean isMovingFromCenter() {
        return internalState[MOVE_FROM_CENTER] == 1f;
    }

    public void setMovingFromCenter(boolean moveFromCenter) {
        setInternalState(MOVE_FROM_CENTER, moveFromCenter ? 1f : 0f);
    }

    public float getScale() {
        return internalState[SCALE];
    }

    public void setScale(float scale) {
        setInternalState(SCALE, scale);
    }

    public boolean isUpdatingScale() {
        return internalState[UPDATE_SCALE] == 1f;
    }

    public float getScaleVar() {
        return internalState[SCALE_VAR];
    }

    public void setScaleVar(float scaleVar) {
        setInternalState(SCALE_VAR, scaleVar);
    }

    public float getEndScale() {
        return internalState[END_SCALE];
    }

    public void setEndScale(float endScale) {
        setInternalState(END_SCALE, endScale);
    }

    public float getEndScaleVar() {
        return internalState[END_SCALE_VAR];
    }

    public void setEndScaleVar(float endScaleVar) {
        setInternalState(END_SCALE_VAR, endScaleVar);
    }

    public float getAccelerationX() {
        return internalState[ACCELERATION_X];
    }

    public void setAccelerationX(float x) {
        setInternalState(ACCELERATION_X, x);
    }

    public float getAccelerationY() {
        return internalState[ACCELERATION_Y];
    }

    public void setAccelerationY(float y) {
        setInternalState(ACCELERATION_Y, y);
    }

    public float getAccelerationXVar() {
        return internalState[ACCELERATION_X_VAR];
    }

    public void setAccelerationXVar(float x) {
        setInternalState(ACCELERATION_X_VAR, x);
    }

    public float getAccelerationYVar() {
        return internalState[ACCELERATION_Y_VAR];
    }

    public void setAccelerationYVar(float y) {
        setInternalState(ACCELERATION_Y_VAR, y);
    }

    public float getCentripetalAcceleration() {
        return internalState[CENTRIPETAL_ACCELERATION];
    }

    public void setCentripetalAcceleration(float centripetalAcc) {
        setInternalState(CENTRIPETAL_ACCELERATION, centripetalAcc);
    }

    public float getCentripetalAccelerationVar() {
        return internalState[CENTRIPETAL_ACCELERATION_VAR];
    }

    public void setCentripetalAccelerationVar(float centripetalAccVar) {
        setInternalState(CENTRIPETAL_ACCELERATION_VAR, centripetalAccVar);
    }

    public float getTangentialAcceleration() {
        return internalState[TANGENTIAL_ACCELERATION];
    }

    public void setTangentialAcceleration(float tangentialAcc) {
        setInternalState(TANGENTIAL_ACCELERATION, tangentialAcc);
    }

    public float getTangentialAccelerationVar() {
        return internalState[TANGENTIAL_ACCELERATION_VAR];
    }

    public void setTangentialAccelerationVar(float tangentialAccVar) {
        setInternalState(TANGENTIAL_ACCELERATION_VAR, tangentialAccVar);
    }

    private void setInternalState(int index, float value) {
        ++updateCounter;
        internalState[index] = value;
    }

    /**
     * The update counter is incremented whenever a parameter is changed.
     */
    public int getUpdateCounter() {
        return updateCounter;
    }

    public boolean isUpdatingColor() {
        return internalState[UPDATE_COLOR] == 1f;
    }

    public float getColorR() {
        return internalState[COLOR_R];
    }

    public void setColorR(float r) {
        setInternalState(COLOR_R, r);
    }

    public float getColorG() {
        return internalState[COLOR_G];
    }

    public void setColorG(float g) {
        setInternalState(COLOR_G, g);
    }

    public float getColorB() {
        return internalState[COLOR_B];
    }

    public void setColorB(float b) {
        setInternalState(COLOR_B, b);
    }

    public float getColorRVar() {
        return internalState[COLOR_R_VAR];
    }

    public void setColorRVar(float r) {
        setInternalState(COLOR_R_VAR, r);
    }

    public float getColorGVar() {
        return internalState[COLOR_G_VAR];
    }

    public void setColorGVar(float g) {
        setInternalState(COLOR_G_VAR, g);
    }

    public float getColorBVar() {
        return internalState[COLOR_B_VAR];
    }

    public void setColorBVar(float b) {
        setInternalState(COLOR_B_VAR, b);
    }

    public float getEndColorR() {
        return internalState[END_COLOR_R];
    }

    public void setEndColorR(float r) {
        setInternalState(END_COLOR_R, r);
    }

    public float getEndColorG() {
        return internalState[END_COLOR_G];
    }

    public void setEndColorG(float g) {
        setInternalState(END_COLOR_G, g);
    }

    public float getEndColorB() {
        return internalState[END_COLOR_B];
    }

    public void setEndColorB(float b) {
        setInternalState(END_COLOR_B, b);
    }

    public float getEndColorRVar() {
        return internalState[END_COLOR_R_VAR];
    }

    public void setEndColorRVar(float r) {
        setInternalState(END_COLOR_R_VAR, r);
    }

    public float getEndColorGVar() {
        return internalState[END_COLOR_G_VAR];
    }

    public void setEndColorGVar(float g) {
        setInternalState(END_COLOR_G_VAR, g);
    }

    public float getEndColorBVar() {
        return internalState[END_COLOR_B_VAR];
    }

    public void setEndColorBVar(float b) {
        setInternalState(END_COLOR_B_VAR, b);
    }

    public float getAlpha() {
        return internalState[ALPHA];
    }

    public void setAlpha(float alpha) {
        setInternalState(ALPHA, alpha);
    }

    public float getAlphaVar() {
        return internalState[ALPHA_VAR];
    }

    public void setAlphaVar(float alphaVar) {
        setInternalState(ALPHA_VAR, alphaVar);
    }

    public float getEndAlpha() {
        return internalState[END_ALPHA];
    }

    public void setEndAlpha(float endAlpha) {
        setInternalState(END_ALPHA, endAlpha);
    }

    public float getEndAlphaVar() {
        return internalState[END_ALPHA_VAR];
    }

    public void setEndAlphaVar(float endAlphaVar) {
        setInternalState(END_ALPHA_VAR, endAlphaVar);
    }

    public void setUpdateAlpha(boolean updateAlpha) {
        setInternalState(UPDATE_ALPHA, updateAlpha ? 1f : 0f);
    }

    public boolean isUpdatingAlpha() {
        return internalState[UPDATE_ALPHA] == 1f;
    }

    public float getRotation() {
        return internalState[ROTATION];
    }

    public void setRotation(float rotation) {
        setInternalState(ROTATION, rotation);
    }

    public float getRotationVar() {
        return internalState[ROTATION_VAR];
    }

    public void setRotationVar(float rotationVar) {
        setInternalState(ROTATION_VAR, rotationVar);
    }

    public float getRotationSpeed() {
        return internalState[ROTATION_SPEED];
    }

    public void setRotationSpeed(float rotationSpeed) {
        setInternalState(ROTATION_SPEED, rotationSpeed);
    }

    public float getRotationSpeedVar() {
        return internalState[ROTATION_SPEED_VAR];
    }

    public void setRotationSpeedVar(float rotationSpeedVar) {
        setInternalState(ROTATION_SPEED_VAR, rotationSpeedVar);
    }

    public float getRotationAcceleration() {
        return internalState[ROTATION_ACCELERATION];
    }

    public void setRotationAcceleration(float rotationAcceleration) {
        setInternalState(ROTATION_ACCELERATION, rotationAcceleration);
    }

    public float getRotationAccelerationVar() {
        return internalState[ROTATION_ACCELERATION_VAR];
    }

    public void setRotationAccelerationVar(float rotationAccelerationVar) {
        setInternalState(ROTATION_ACCELERATION_VAR, rotationAccelerationVar);
    }

    public void scale(float factor) {
        setSpeed(getSpeed() * factor);
        setSpeedVar(getSpeedVar() * factor);
        setScale(getScale() * factor);
        setScaleVar(getScaleVar() * factor);
        setEndScale(getEndScale() * factor);
        setEndScaleVar(getEndScaleVar() * factor);
        setAcceleration(getAccelerationX() * factor, getAccelerationY() * factor);
        setAccelerationVar(getAccelerationXVar() * factor, getAccelerationYVar() * factor);
        setCentripetalAcceleration(getCentripetalAcceleration() * factor);
        setCentripetalAccelerationVar(getCentripetalAccelerationVar() * factor);
        setTangentialAcceleration(getTangentialAcceleration() * factor);
        setTangentialAccelerationVar(getTangentialAccelerationVar() * factor);
    }

    public void apply(Particle particle, float x, float y, double spawnAngle) {
        if (isIndividualGravityCenter()) {
            particle.setGravityCenter(particle.getPosX(), particle.getPosY());
        } else {
            particle.setGravityCenter(x, y);
        }

        float emittedLife = getLife() + getLifeVar() * RandomInstance.random11();

        float emittedColor0 = getColorR() + getColorRVar() * RandomInstance.random11();
        float emittedColor1 = getColorG() + getColorGVar() * RandomInstance.random11();
        float emittedColor2 = getColorB() + getColorBVar() * RandomInstance.random11();
        float emittedAlpha = getAlpha() + getAlphaVar() * RandomInstance.random11();

        float emittedScale = getScale() + getScaleVar() * RandomInstance.random11();

        float emittedSpeed = getSpeed() + getSpeedVar() * RandomInstance.random11();

        float velocityX = 0, velocityY = 0;
        if (emittedSpeed != 0) {
            if (isMovingFromCenter()) {
                velocityX = (float) Math.cos(spawnAngle) * emittedSpeed;
                velocityY = (float) -Math.sin(spawnAngle) * emittedSpeed;
            } else {
                velocityY = emittedSpeed;
            }

            float direction = getDirection() + getDirectionVar() * RandomInstance.random11();
            double directionRadians = direction * Math.PI / 180d;
            double sin = Math.sin(directionRadians);
            double cos = Math.cos(directionRadians);
            float velocityX0 = velocityX;
            velocityX = (float) (velocityX0 * cos + velocityY * sin);
            velocityY = (float) (-velocityX0 * sin + velocityY * cos);
        }

        float endSpeedFactor = getEndSpeedFactor() + getEndSpeedFactorVar() * RandomInstance.random11();

        particle.setScale(emittedScale);
        particle.setRotation(getRotation() + getRotationVar() * RandomInstance.random11());

        particle.setColor(emittedColor0 / 255f, emittedColor1 / 255f, emittedColor2 / 255f, emittedAlpha);

        particle.setDelay(getDelay() + getDelayVar() * RandomInstance.random11());

        particle.setLife(emittedLife);
        particle.setEndSpeedFactor(endSpeedFactor);
        particle.setVelocity(velocityX, velocityY);

        particle.setAccelerationX(
                getAccelerationX() + getAccelerationXVar() * RandomInstance.random11());

        particle.setAccelerationY(
                getAccelerationY() + getAccelerationYVar() * RandomInstance.random11());

        particle.setCentripetalAcceleration(getCentripetalAcceleration() + getCentripetalAccelerationVar() * RandomInstance.random11());
        particle.setTangentialAcceleration(getTangentialAcceleration() + getTangentialAccelerationVar() * RandomInstance.random11());

        if (isRotatedForward()) {
            particle.setRotatedForward(true);
        } else {
            particle.setRotatedForward(false);
            particle.setRotationSpeed(getRotationSpeed() + getRotationSpeedVar() * RandomInstance.random11());
            particle.setRotationAcceleration(getRotationAcceleration() + getRotationAccelerationVar() * RandomInstance.random11());
        }

        if (isUpdatingScale()) {
            float randomizedEndScaleVar = getEndScaleVar() * RandomInstance.random11();
            particle.setDeltaScale(((getEndScale() + randomizedEndScaleVar) - emittedScale) / emittedLife);
        } else {
            particle.setDeltaScale(0);
        }

        float deltaAlpha = isUpdatingAlpha()
                ? ((getEndAlpha() + getEndAlphaVar() * RandomInstance.random11()) - emittedAlpha) / emittedLife
                : 0;

        if (isUpdatingColor()) {
            particle.setDeltaColor(
                    ((getEndColorR() + getEndColorRVar() * RandomInstance.random11()) - emittedColor0)
                            / (255f * emittedLife),
                    ((getEndColorG() + getEndColorGVar() * RandomInstance.random11()) - emittedColor1)
                            / (255f * emittedLife),
                    ((getEndColorB() + getEndColorBVar() * RandomInstance.random11()) - emittedColor2)
                            / (255f * emittedLife),
                    deltaAlpha);
        } else {
            particle.setDeltaColor(0, 0, 0, deltaAlpha);
        }

        particle.setTime(0f);
    }
}