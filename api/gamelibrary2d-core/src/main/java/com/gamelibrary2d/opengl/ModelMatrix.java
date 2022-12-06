package com.gamelibrary2d.opengl;

import com.gamelibrary2d.CoordinateSpace;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModelMatrix {
    private static ModelMatrix instance;
    private final List<float[]> bufferStack = new ArrayList<>();
    private final InternalMatrixBuffer matrixBuffer = new InternalMatrixBuffer();

    private int stackPointer = -1;

    private ModelMatrix() {
        instance = this;
    }

    public static ModelMatrix instance() {
        return instance != null ? instance : new ModelMatrix();
    }

    public void clearMatrix() {
        matrixBuffer.clear();
    }

    public void pushMatrix() {
        ++stackPointer;
        float[] stored;
        if (stackPointer == bufferStack.size()) {
            stored = new float[16];
            bufferStack.add(stored);
        } else {
            stored = bufferStack.get(stackPointer);
        }
        matrixBuffer.serialize(stored);
    }

    public void popMatrix() {
        if (stackPointer == -1) return;
        float[] stored = bufferStack.get(stackPointer);
        matrixBuffer.deserialize(stored);
        --stackPointer;
    }

    public void transform(CoordinateSpace coordinateSpace) {
        translatef(
                coordinateSpace.getPosX() + coordinateSpace.getScaleAndRotationAnchorX(),
                coordinateSpace.getPosY() + coordinateSpace.getScaleAndRotationAnchorY(),
                0);

        rotatef(-coordinateSpace.getRotation(), 0, 0, 1);

        scalef(coordinateSpace.getScaleX(), coordinateSpace.getScaleY(), 1.0f);
    }

    public void translatef(float x, float y, float z) {
        matrixBuffer.translate(x, y, z);
    }

    public void scalef(float x, float y, float z) {
        if (x != 1 || y != 1 || z != 1) {
            matrixBuffer.scale(x, y, z);
        }
    }

    /**
     * Rotation is clockwise in degrees around the specified axis.
     */
    public void rotatef(float angle, float x, float y, float z) {
        if (angle != 0) {
            if (z == 1) {
                matrixBuffer.rotateZ(angle);
            } else {
                matrixBuffer.rotate(angle, x, y, z);
            }
        }
    }

    public FloatBuffer getFloatBuffer() {
        return matrixBuffer.getFloatBuffer();
    }
}