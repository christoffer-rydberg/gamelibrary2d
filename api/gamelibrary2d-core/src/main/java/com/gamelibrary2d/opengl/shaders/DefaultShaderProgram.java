package com.gamelibrary2d.opengl.shaders;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.disposal.Disposable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.BufferUtils;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class DefaultShaderProgram implements ShaderProgram, Disposable {
    private final static String PARAMETERS_ATTRIBUTE = "parameters";
    private final static int LOCATION_NOT_FOUND = Integer.MIN_VALUE;

    private final FloatBuffer parameters = BufferUtils.createFloatBuffer(ShaderParameter.MAX_PARAMETERS);
    private final Map<CharSequence, Integer> uniformLocations = new HashMap<>();
    private final Map<CharSequence, Integer> attributeLocations = new HashMap<>();
    private final int programId;

    private boolean initialized;
    private int uniModel;
    private int parametersLocation;

    private DefaultShaderProgram(Disposer disposer) {
        programId = OpenGL.instance().glCreateProgram();
        disposer.registerDisposal(this);
    }

    public static DefaultShaderProgram create(Disposer disposer) {
        return new DefaultShaderProgram(disposer);
    }

    public void initialize() {
        if (initialized)
            return;

        OpenGL.instance().glLinkProgram(programId);

        // Check that the program was linked successfully.
        int status = OpenGL.instance().glGetProgrami(programId, OpenGL.GL_LINK_STATUS);
        if (status != OpenGL.GL_TRUE) {
            String programInfoLog = OpenGL.instance().glGetProgramInfoLog(programId);
            throw new IllegalStateException(programInfoLog);
        }

        initialized = true;
    }

    public void initializeMvp(float windowWidth, float windowHeight) {
        if (!initialized) {
            throw new IllegalStateException("The shader program has not been initialized.");
        }

        bind();

        // Set the model matrix
        FloatBuffer modelMatrix = createIdentityMatrix();
        uniModel = OpenGL.instance().glGetUniformLocation(programId, "model");
        OpenGL.instance().glUniformMatrix4fv(uniModel, false, modelMatrix);

        // Set the view matrix
        FloatBuffer viewMatrix = createIdentityMatrix();
        int uniView = OpenGL.instance().glGetUniformLocation(programId, "view");
        OpenGL.instance().glUniformMatrix4fv(uniView, false, viewMatrix);

        // Set the projection matrix
        FloatBuffer projectionMatrix = createProjectionMatrix(0, windowWidth, 0, windowHeight, 1f, -1f);
        int uniProjection = OpenGL.instance().glGetUniformLocation(programId, "projection");
        OpenGL.instance().glUniformMatrix4fv(uniProjection, false, projectionMatrix);

        parametersLocation = OpenGL.instance().glGetUniformLocation(programId, PARAMETERS_ATTRIBUTE);
    }

    private static FloatBuffer createIdentityMatrix() {
        float[] identity = new float[16];
        identity[0] = 1;
        identity[5] = 1;
        identity[10] = 1;
        identity[15] = 1;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 16);
        buffer.put(identity);
        buffer.flip();

        return buffer;
    }

    private static FloatBuffer createProjectionMatrix(float left, float windowWidth, float bottom, float top, float near, float far) {
        float[] projection = new float[16];
        float tx = -(windowWidth + left) / (windowWidth - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);
        projection[0] = 2f / (windowWidth - left);
        projection[5] = 2f / (top - bottom);
        projection[10] = -2f / (far - near);
        projection[12] = tx;
        projection[13] = ty;
        projection[14] = tz;
        projection[15] = 1;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(4 * 16);
        buffer.put(projection);
        buffer.flip();

        return buffer;
    }

    @Override
    public void dispose() {
        unbind(); // Important for cleanup, delete is not enough.
        OpenGL.instance().glDeleteProgram(programId);
        initialized = false;
    }

    public int getProgramId() {
        return programId;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void attachShader(Shader shader) {
        OpenGL.instance().glAttachShader(programId, shader.getId());
    }

    public void detachShader(Shader shader) {
        OpenGL.instance().glDetachShader(programId, shader.getId());
    }

    public int getAttributeLocation(CharSequence name) {
        int location = attributeLocations.getOrDefault(name, LOCATION_NOT_FOUND);
        if (location == LOCATION_NOT_FOUND) {
            location = OpenGL.instance().glGetAttribLocation(programId, name);
            attributeLocations.put(name, location);
        }

        return location;
    }

    public int getUniformLocation(CharSequence name) {
        int location = uniformLocations.getOrDefault(name, LOCATION_NOT_FOUND);
        if (location == LOCATION_NOT_FOUND) {
            location = OpenGL.instance().glGetUniformLocation(programId, name);
            uniformLocations.put(name, location);
        }

        return location;
    }

    public float getParameter(int index) {
        return parameters.get(index);
    }

    public boolean setParameter(int index, float value) {
        if (value == getParameter(index))
            return false;
        parameters.put(index, value);
        return true;
    }

    public void setParameters(float[] params, int offset, int length) {
        this.parameters.clear();
        this.parameters.put(params, offset, length);
        this.parameters.flip();
    }

    public void applyParameters() {
        OpenGL.instance().glUniform1fv(parametersLocation, this.parameters);
    }

    public void updateModelMatrix() {
        OpenGL.instance().glUniformMatrix4fv(uniModel, false, ModelMatrix.instance().getFloatBuffer());
    }

    public void bind() {
        OpenGLState.useProgram(programId);
    }

    public void unbind() {
        OpenGLState.useProgram(0);
    }
}