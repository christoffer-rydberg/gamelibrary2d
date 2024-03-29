package com.gamelibrary2d.opengl.buffers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.BufferUtils;

import java.nio.IntBuffer;

public class MirroredIntBuffer extends AbstractMirroredBuffer {
    private final int usage;

    private int[] data;
    private IntBuffer ioBuffer;

    private MirroredIntBuffer(int target, int usage) {
        super(target);
        this.usage = usage;
    }

    public static MirroredIntBuffer create(int[] data, int target, int usage, Disposer disposer) {
        MirroredIntBuffer buffer = new MirroredIntBuffer(target, usage);
        buffer.allocate(data);
        disposer.registerDisposal(buffer);
        return buffer;
    }

    public int[] getData() {
        return data;
    }

    public boolean allocate(int[] data) {
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
                ioBuffer = BufferUtils.createIntBuffer(data.length);
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
            OpenGL.instance().glBufferSubData(target, offset * Integer.BYTES, ioBuffer);
        }
    }

    @Override
    protected void onUpdateCPU(int target, int offset, int len) {
        if (len > 0) {
            ioBuffer.clear();
            ioBuffer.limit(len);
            OpenGL.instance().glGetBufferSubData(target, offset * Integer.BYTES, ioBuffer);
            for (int i = offset; i < len; ++i) {
                data[i] = ioBuffer.get(i);
            }
        }
    }
}