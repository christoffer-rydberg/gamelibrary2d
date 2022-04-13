package com.gamelibrary2d.particles;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;
import com.gamelibrary2d.common.random.RandomInstance;

public class ParticleSpawnParameters implements Serializable {
    private static final int STRIDE = 8;

    private static final int ANGLE = 0;
    private static final int ANGLE_VAR = 1;
    private static final int RADIUS_X = 2;
    private static final int RADIUS_X_VAR = 3;

    private static final int RADIUS_Y = 4;
    private static final int RADIUS_Y_VAR = 5;
    private static final int OFFSET_X_VAR = 6;
    private static final int OFFSET_Y_VAR = 7;

    private float offsetX;
    private float offsetY;

    private final float[] internalState = new float[STRIDE];

    private int updateCounter;

    public ParticleSpawnParameters() {
        internalState[ANGLE_VAR] = 180;
    }

    public ParticleSpawnParameters(DataBuffer buffer) {
        for (int i = 0; i < STRIDE; ++i) {
            internalState[i] = buffer.getFloat();
        }
    }

    @Override
    public final void serialize(DataBuffer buffer) {
        for (int i = 0; i < STRIDE; ++i) {
            buffer.putFloat(internalState[i]);
        }
    }

    public float[] getInternalStateArray() {
        return internalState;
    }

    private void setInternalState(int index, float value) {
        if (internalState[index] != value) {
            internalState[index] = value;
            ++updateCounter;
        }
    }

    /**
     * The update counter is incremented whenever a parameter is changed.
     */
    public int getUpdateCounter() {
        return updateCounter;
    }

    public float getAngle() {
        return internalState[ANGLE];
    }

    public void setAngle(float angle) {
        setInternalState(ANGLE, angle);
    }

    public float getAngleVar() {
        return internalState[ANGLE_VAR];
    }

    public void setAngleVar(float angleVar) {
        setInternalState(ANGLE_VAR, angleVar);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetXVar() {
        return internalState[OFFSET_X_VAR];
    }

    public void setOffsetXVar(float offsetXVar) {
        setInternalState(OFFSET_X_VAR, offsetXVar);
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getOffsetYVar() {
        return internalState[OFFSET_Y_VAR];
    }

    public void setOffsetYVar(float offsetYVar) {
        setInternalState(OFFSET_Y_VAR, offsetYVar);
    }

    public float getRadiusX() {
        return internalState[RADIUS_X];
    }

    public void setRadiusX(float radiusX) {
        setInternalState(RADIUS_X, radiusX);
    }

    public float getRadiusXVar() {
        return internalState[RADIUS_X_VAR];
    }

    public void setRadiusXVar(float radiusXVar) {
        setInternalState(RADIUS_X_VAR, radiusXVar);
    }

    public float getRadiusY() {
        return internalState[RADIUS_Y];
    }

    public void setRadiusY(float radiusY) {
        setInternalState(RADIUS_Y, radiusY);
    }

    public float getRadiusYVar() {
        return internalState[RADIUS_Y_VAR];
    }

    public void setRadiusYVar(float radiusYVar) {
        setInternalState(RADIUS_Y_VAR, radiusYVar);
    }

    public void scale(float factor) {
        setOffsetXVar(getOffsetXVar() * factor);
        setOffsetYVar(getOffsetYVar() * factor);
        setRadiusX(getRadiusX() * factor);
        setRadiusXVar(getRadiusXVar() * factor);
        setRadiusY(getRadiusY() * factor);
        setRadiusYVar(getRadiusYVar() * factor);
    }

    public double apply(Particle particle, float x, float y) {
        float posX = 0f, posY = 0f;

        float radiusX = getRadiusX();
        float radiusXVar = getRadiusXVar();
        float radiusY = getRadiusY();
        float radiusYVar = getRadiusYVar();
        if (radiusX != 0f || radiusXVar != 0f || radiusY != 0f || radiusYVar != 0f) {
            float angle = getAngle() - getAngleVar() * RandomInstance.random11() - 90;
            double angleRadians = angle * Math.PI / 180d;

            double alpha = RandomInstance.get().nextDouble();

            double minRx = radiusX - radiusXVar;
            double maxRx = radiusX + radiusXVar;
            double rx = minRx * (1.0 - alpha) + maxRx * alpha;

            double minRy = radiusY - radiusYVar;
            double maxRy = radiusY + radiusYVar;
            double ry = minRy * (1.0 - alpha) + maxRy * alpha;

            posX += (float) (Math.cos(angleRadians) * rx);
            posY += (float) (-Math.sin(angleRadians) * ry);
        }

        float offsetXVar = getOffsetXVar();
        float offsetYVar = getOffsetYVar();
        if (offsetXVar != 0f || offsetYVar != 0f) {
            posX += offsetXVar * RandomInstance.random11();
            posY += offsetYVar * RandomInstance.random11();
        }

        particle.setPosition(x + getOffsetX() + posX, y + getOffsetY() + posY);

        return Math.atan2(posX, posY) - Math.PI / 2.0;
    }
}