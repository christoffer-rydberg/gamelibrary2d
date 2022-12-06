package com.gamelibrary2d.opengl;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Runtime;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class OpenGLState {
    private static int boundTexture = 0;
    private static int boundFrameBuffer = 0;
    private static int boundProgram = -1;
    private static BlendMode boundBlendMode;

    private static ShaderProgram primaryShaderProgram;
    private static ShaderProgram primaryParticleUpdaterProgram;
    private static ShaderProgram pointParticleShaderProgram;
    private static ShaderProgram quadParticleShaderProgram;
    private static ShaderProgram pointShaderProgram;
    private static ShaderProgram quadShaderProgram;

    public static ShaderProgram getPrimaryParticleUpdaterProgram() {
        if (primaryParticleUpdaterProgram == null) {
            String openGlVersion = Runtime.getFramework().getOpenGL().getSupportedVersion().toString();
            throw new RuntimeException(String.format("%s does not support support compute shaders", openGlVersion));
        } else {
            return primaryParticleUpdaterProgram;
        }
    }

    public static void setPrimaryParticleUpdaterProgram(ShaderProgram primaryParticleUpdaterProgram) {
        OpenGLState.primaryParticleUpdaterProgram = primaryParticleUpdaterProgram;
    }

    public static ShaderProgram getPrimaryShaderProgram() {
        return primaryShaderProgram;
    }

    public static void setPrimaryShaderProgram(ShaderProgram shaderProgram) {
        primaryShaderProgram = shaderProgram;
    }

    public static ShaderProgram getPointParticleShaderProgram() {
        if (pointParticleShaderProgram == null) {
            String openGlVersion = Runtime.getFramework().getOpenGL().getSupportedVersion().toString();
            throw new RuntimeException(String.format("%s does not support support geometry shaders", openGlVersion));
        } else {
            return pointParticleShaderProgram;
        }
    }

    public static void setPointParticleShaderProgram(ShaderProgram shaderProgram) {
        pointParticleShaderProgram = shaderProgram;
    }

    public static ShaderProgram getQuadParticleShaderProgram() {
        if (quadParticleShaderProgram == null) {
            String openGlVersion = Runtime.getFramework().getOpenGL().getSupportedVersion().toString();
            throw new RuntimeException(String.format("%s does not support support geometry shaders", openGlVersion));
        } else {
            return quadParticleShaderProgram;
        }
    }

    public static void setQuadParticleShaderProgram(ShaderProgram shaderProgram) {
        quadParticleShaderProgram = shaderProgram;
    }

    public static ShaderProgram getPointShaderProgram() {
        return pointShaderProgram;
    }

    public static void setPointShaderProgram(ShaderProgram shaderProgram) {
        pointShaderProgram = shaderProgram;
    }

    public static ShaderProgram getQuadShaderProgram() {
        return quadShaderProgram;
    }

    public static void setQuadShaderProgram(ShaderProgram shaderProgram) {
        quadShaderProgram = shaderProgram;
    }

    public static int bindTexture(int id) {
        int previous = boundTexture;

        if (previous != id) {
            OpenGL.instance().glBindTexture(OpenGL.GL_TEXTURE_2D, id);
            boundTexture = id;
        }

        return previous;
    }

    public static void unbindTexture(int id) {
        if (boundTexture != id) {
            OpenGL.instance().glBindTexture(OpenGL.GL_TEXTURE_2D, 0);
            boundTexture = 0;
        }
    }

    public static int bindFrameBuffer(int id) {
        int previous = boundFrameBuffer;

        if (previous != id) {
            OpenGL.instance().glBindFramebuffer(OpenGL.GL_FRAMEBUFFER, id);
            boundFrameBuffer = id;
        }

        return previous;
    }

    public static void unbindFrameBuffer(int id) {
        if (boundFrameBuffer != id) {
            OpenGL.instance().glBindFramebuffer(OpenGL.GL_FRAMEBUFFER, 0);
            boundFrameBuffer = 0;
        }
    }

    public static void useProgram(int program) {
        if (boundProgram != program) {
            OpenGL.instance().glUseProgram(program);
            boundProgram = program;
        }
    }

    public static BlendMode getBlendMode() {
        return boundBlendMode;
    }

    public static void setBlendMode(BlendMode blendMode) {
        if (blendMode == boundBlendMode) {
            return;
        }

        boolean blendEnabled = boundBlendMode != null && boundBlendMode != BlendMode.NONE;

        boundBlendMode = blendMode;

        if (blendMode == null)
            return;

        OpenGL openGL = OpenGL.instance();

        switch (blendMode) {
            case NONE:
                openGL.glDisable(OpenGL.GL_BLEND);
                break;

            case TRANSPARENT:
                if (!blendEnabled) {
                    openGL.glEnable(OpenGL.GL_BLEND);
                }
                openGL.glBlendFunc(OpenGL.GL_SRC_ALPHA, OpenGL.GL_ONE_MINUS_SRC_ALPHA);
                break;

            case ADDITIVE:
                if (!blendEnabled) {
                    openGL.glEnable(OpenGL.GL_BLEND);
                }
                openGL.glBlendFunc(OpenGL.GL_SRC_ALPHA, OpenGL.GL_ONE);
                break;

            case MASKED:
                if (!blendEnabled) {
                    openGL.glEnable(OpenGL.GL_BLEND);
                }
                openGL.glBlendFunc(OpenGL.GL_ZERO, OpenGL.GL_ONE_MINUS_SRC_ALPHA);
                break;
        }
    }
}
