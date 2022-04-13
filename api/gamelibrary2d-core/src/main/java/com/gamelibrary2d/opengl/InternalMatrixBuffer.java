package com.gamelibrary2d.opengl;

import com.gamelibrary2d.common.io.BufferUtils;

import java.nio.FloatBuffer;

class InternalMatrixBuffer {
    private final float[] id = createIdentityMatrix();
    private final float[] current = new float[16];
    private final float[] result = new float[16];
    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 16);

    InternalMatrixBuffer() {
        clear();
    }

    private static float[] createIdentityMatrix() {
        float[] identity = new float[16];
        identity[0] = 1;
        identity[5] = 1;
        identity[10] = 1;
        identity[15] = 1;
        return identity;
    }

    void clear() {
        deserialize(id);
    }

    void serialize(float[] array) {
        copyValues(current, array);
    }

    void deserialize(float[] array) {
        copyValues(array, current);
    }

    FloatBuffer getFloatBuffer() {
        buffer.clear();
        buffer.put(current);
        buffer.flip();
        return buffer;
    }

    void translate(float x, float y, float z) {
        current[12] = current[0] * x + current[4] * y + current[8] * z + current[12];
        current[13] = current[1] * x + current[5] * y + current[9] * z + current[13];
        current[14] = current[2] * x + current[6] * y + current[10] * z + current[14];
        current[15] = current[3] * x + current[7] * y + current[11] * z + current[15];
    }

    void scale(float x, float y, float z) {
        current[0] *= x;
        current[1] *= x;
        current[2] *= x;
        current[3] *= x;
        current[4] *= y;
        current[5] *= y;
        current[6] *= y;
        current[7] *= y;
        current[8] *= z;
        current[9] *= z;
        current[10] *= z;
        current[11] *= z;
    }

    void rotateZ(float angle) {
        double angleRadians = Math.toRadians(angle);
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);

        float other00 = c;
        float other01 = s;
        float other10 = -s;
        float other11 = c;

        result[0] = current[0] * other00 + current[4] * other01;
        result[1] = current[1] * other00 + current[5] * other01;
        result[2] = current[2] * other00 + current[6] * other01;
        result[3] = current[3] * other00 + current[7] * other01;

        result[4] = current[0] * other10 + current[4] * other11;
        result[5] = current[1] * other10 + current[5] * other11;
        result[6] = current[2] * other10 + current[6] * other11;
        result[7] = current[3] * other10 + current[7] * other11;

        System.arraycopy(result, 0, current, 0, 8);
    }

    void rotate(float angle, float x, float y, float z) {
        double angleRadians = Math.toRadians(angle);
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);

        float other00 = x * x * (1f - c) + c;
        float other01 = y * x * (1f - c) + z * s;
        float other02 = x * z * (1f - c) - y * s;
        float other10 = x * y * (1f - c) - z * s;
        float other11 = y * y * (1f - c) + c;
        float other12 = y * z * (1f - c) + x * s;
        float other20 = x * z * (1f - c) + y * s;
        float other21 = y * z * (1f - c) - x * s;
        float other22 = z * z * (1f - c) + c;

        result[0] = current[0] * other00 + current[4] * other01 + current[8] * other02;
        result[1] = current[1] * other00 + current[5] * other01 + current[9] * other02;
        result[2] = current[2] * other00 + current[6] * other01 + current[10] * other02;
        result[3] = current[3] * other00 + current[7] * other01 + current[11] * other02;

        result[4] = current[0] * other10 + current[4] * other11 + current[8] * other12;
        result[5] = current[1] * other10 + current[5] * other11 + current[9] * other12;
        result[6] = current[2] * other10 + current[6] * other11 + current[10] * other12;
        result[7] = current[3] * other10 + current[7] * other11 + current[11] * other12;

        result[8] = current[0] * other20 + current[4] * other21 + current[8] * other22;
        result[9] = current[1] * other20 + current[5] * other21 + current[9] * other22;
        result[10] = current[2] * other20 + current[6] * other21 + current[10] * other22;
        result[11] = current[3] * other20 + current[7] * other21 + current[11] * other22;

        System.arraycopy(result, 0, current, 0, 12);
    }

    private void copyValues(float[] from, float[] to) {
        System.arraycopy(from, 0, to, 0, 16);
    }
}