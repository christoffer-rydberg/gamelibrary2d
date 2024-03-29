package com.gamelibrary2d.opengl.buffers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.BufferUtils;

import java.nio.FloatBuffer;

public class MirroredFloatBuffer extends AbstractMirroredBuffer {
    private final int usage;

    private float[] data;
    private FloatBuffer ioBuffer;

    private MirroredFloatBuffer(int target, int usage) {
        super(target);
        this.usage = usage;
    }

    public static MirroredFloatBuffer create(float[] data, int target, int usage, Disposer disposer) {
        MirroredFloatBuffer buffer = new MirroredFloatBuffer(target, usage);
        buffer.allocate(data);
        disposer.registerDisposal(buffer);
        return buffer;
    }

    public float[] getData() {
        return data;
    }

    public boolean allocate(float[] data) {
        if (this.data != data) {
            this.data = data;
            allocate();
            return true;
        }

        return false;
    }

    @Override
    public void copy(int offset, int destination, int len) {
        System.arraycopy(data, offset, data, destination, len);
    }

    @Override
    public int getCapacity() {
        return data.length;
    }

    @Override
    protected void onAllocate(int target) {
        if (data.length > 0) {
            if (ioBuffer == null || ioBuffer.capacity() != data.length) {
                ioBuffer = BufferUtils.createFloatBuffer(data.length);
                ioBuffer.put(data);
                ioBuffer.flip();
                OpenGL.instance().glBufferData(target, ioBuffer, usage);
            } else {
                ioBuffer.clear();
                ioBuffer.put(data);
                ioBuffer.flip();
                OpenGL.instance().glBufferSubData(target, 0, ioBuffer);
            }
        }
    }

    @Override
    protected void onUpdateGPU(int target, int offset, int len) {
        if (len > 0) {
            ioBuffer.clear();
            ioBuffer.put(data, offset, len);
            ioBuffer.flip();
            OpenGL.instance().glBufferSubData(target, offset * Float.BYTES, ioBuffer);
        }
    }

    @Override
    protected void onUpdateCPU(int target, int offset, int len) {
        if (len > 0) {
            ioBuffer.clear();
            ioBuffer.limit(len);
            OpenGL.instance().glGetBufferSubData(target, offset * Float.BYTES, ioBuffer);
            for (int i = offset; i < len; ++i) {
                data[i] = ioBuffer.get(i);
            }
        }
    }
}