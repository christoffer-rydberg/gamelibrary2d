package com.gamelibrary2d.opengl.shaders;

import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.framework.Runtime;

public class DefaultShader implements Shader, Disposable {
    private final int id;
    private boolean disposed;

    private DefaultShader(CharSequence source, int type) {
        id = OpenGL.instance().glCreateShader(type);
        OpenGL.instance().glShaderSource(id, source);
        OpenGL.instance().glCompileShader(id);
        checkCompileStatus();
    }

    private static void appendHeaders(OpenGL.OpenGLVersion supportedVersion, ShaderType shaderType, StringBuilder builder) {
        switch (supportedVersion) {
            case OPENGL_ES_3:
                builder.append("#version 300 es").append("\n");
                builder.append("precision mediump float;").append("\n");
                break;
            case OPENGL_ES_3_1:
                if (shaderType == ShaderType.GEOMETRY) {
                    builder.append("#extension GL_OES_geometry_shader : require").append("\n");
                    builder.append("#extension GL_OES_shader_io_blocks : require").append("\n");
                }
                builder.append("#version 310 es").append("\n");
                builder.append("precision mediump float;").append("\n");
                break;
            case OPENGL_ES_3_2:
                builder.append("#version 320 es").append("\n");
                builder.append("precision mediump float;").append("\n");
                break;
            case OPENGL_CORE_430:
                builder.append("#version 430 core").append("\n");
                break;
        }
    }

    public static DefaultShader create(String src, ShaderType shaderType, Disposer disposer) {
        StringBuilder builder = new StringBuilder();

        appendHeaders(
                Runtime.getFramework().getOpenGL().getSupportedVersion(),
                shaderType,
                builder);

        builder.append(src);

        DefaultShader shader = createShader(builder.toString(), shaderType);
        disposer.registerDisposal(shader);
        return shader;
    }

    private static DefaultShader createShader(String src, ShaderType shaderType) {
        switch (shaderType) {
            case COMPUTE:
                return new DefaultShader(src, OpenGL.GL_COMPUTE_SHADER);
            case FRAGMENT:
                return new DefaultShader(src, OpenGL.GL_FRAGMENT_SHADER);
            case GEOMETRY:
                return new DefaultShader(src, OpenGL.GL_GEOMETRY_SHADER);
            case VERTEX:
                return new DefaultShader(src, OpenGL.GL_VERTEX_SHADER);
            default:
                throw new IllegalStateException("Unexpected value: " + shaderType);
        }
    }

    private void checkCompileStatus() {
        int status = OpenGL.instance().glGetShaderi(id, OpenGL.GL_COMPILE_STATUS);
        if (status != OpenGL.GL_TRUE) {
            String shaderInfoLog = OpenGL.instance().glGetShaderInfoLog(id);
            throw new IllegalStateException(shaderInfoLog);
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            OpenGL.instance().glDeleteShader(id);
            disposed = true;
        }
    }

    public boolean isDisposed() {
        return disposed;
    }
}