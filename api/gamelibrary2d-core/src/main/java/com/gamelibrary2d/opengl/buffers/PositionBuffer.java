package com.gamelibrary2d.opengl.buffers;

import com.gamelibrary2d.FloatArrayList;
import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.disposal.Disposer;

public class PositionBuffer extends AbstractMirroredVertexArrayBuffer<MirroredFloatBuffer> {
    private final static int STRIDE = 2;
    private final static int ELEMENT_SIZE = 2;
    private final FloatArrayList data;

    private PositionBuffer(FloatArrayList data, MirroredFloatBuffer buffer) {
        super(buffer, STRIDE, ELEMENT_SIZE);
        this.data = data;
    }

    public static PositionBuffer create(Disposer disposer) {
        return create(new FloatArrayList(), disposer);
    }

    public static PositionBuffer create(float[] data, Disposer disposer) {
        return create(new FloatArrayList(data), disposer);
    }

    public static PositionBuffer create(FloatArrayList data, Disposer disposer) {
        MirroredFloatBuffer buffer = MirroredFloatBuffer.create(
                data.getInternalArray(), OpenGL.GL_ARRAY_BUFFER, OpenGL.GL_DYNAMIC_DRAW, disposer);
        PositionBuffer positionBuffer = new PositionBuffer(data, buffer);
        disposer.registerDisposal(positionBuffer);
        return positionBuffer;
    }

    public int size() {
        return data.size() / STRIDE;
    }

    public FloatArrayList getData() {
        return data;
    }

    public void clear() {
        data.clear();
    }

    public void add(float x, float y) {
        data.add(x);
        data.add(y);
        updateGPU(size() - 1, 1);
    }

    public void add(Point point) {
        add(point.getX(), point.getY());
    }

    public void add(int index, float x, float y) {
        int bufferIndex = index * STRIDE;
        data.add(bufferIndex, x);
        data.add(bufferIndex + 1, y);
        updateGPU(index, size() - index);
    }

    public void add(int index, Point point) {
        add(index, point.getX(), point.getY());
    }

    public void set(int index, float x, float y) {
        int bufferIndex = index * STRIDE;
        data.set(bufferIndex, x);
        data.set(bufferIndex + 1, y);
        updateGPU(index, 1);
    }

    public void set(int index, Point point) {
        set(index, point.getX(), point.getY());
    }

    public void remove(int index) {
        int bufferIndex = index * STRIDE;
        data.remove(bufferIndex);
        data.remove(bufferIndex);
        updateGPU(index, size() - index);
    }

    @Override
    public void updateGPU(int offset, int len) {
        int requiredSize = offset + len;

        int size = size();
        if (requiredSize > size) {
            throw new IndexOutOfBoundsException("Index: " + (requiredSize - 1) + ", Size: " + size);
        }

        MirroredFloatBuffer buffer = getBuffer();
        float[] bufferData = buffer.getData();
        if (requiredSize <= bufferData.length / STRIDE) {
            super.updateGPU(offset, len);
        } else {
            // Assuming update after array growth. Reallocate entire OpenGL buffer:
            getBuffer().allocate(data.getInternalArray());
        }
    }

    @Override
    public void updateCPU(int offset, int len) {
        super.updateCPU(offset, len);
        float[] bufferData = getBuffer().getData();
        float[] internalData = data.getInternalArray();
        if (bufferData != internalData) {
            int bufferOffset = offset * STRIDE;
            int bufferLength = len * STRIDE;
            int requiredSize = bufferOffset + bufferLength;
            if (internalData.length >= requiredSize) {
                System.arraycopy(bufferData, bufferOffset, internalData, bufferOffset, bufferLength);
            } else {
                throw new IndexOutOfBoundsException("Index: " + (requiredSize - 1) + ", Size: " + internalData.length);
            }
        }
    }

    public float getX(int index) {
        return data.get(index * STRIDE);
    }

    public float getY(int index) {
        return data.get(index * STRIDE + 1);
    }
}