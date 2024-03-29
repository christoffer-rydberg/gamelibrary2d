package com.gamelibrary2d.lightning;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.BufferUtils;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.shaders.DefaultShader;
import com.gamelibrary2d.opengl.shaders.DefaultShaderProgram;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DefaultLightRenderer implements LightRenderer {
    private static final int MAXIMUM_LIGHT = 127;
    private static final int PARAMETER_ALPHA_MAP_COLS = ShaderParameter.MIN_PARAMETERS;
    private static final int PARAMETER_ALPHA_MAP_ROWS = ShaderParameter.MIN_PARAMETERS + 1;

    private final Point position = new Point();
    private final List<LightMap> lightMaps = new ArrayList<>();
    private final int windowWidth;
    private final int windowHeight;

    private final int textureId;
    private final SurfaceRenderer<Quad> renderer;
    private final DefaultDisposer resolutionChangedDisposer;

    private ByteBuffer transparencyTexture;
    private int alphaMapCols;
    private int alphaMapRows;

    private boolean prepared;
    private int colOffset;
    private int colSpan;
    private int rowOffset;
    private int rowSpan;
    private float gameCellWidth;
    private float gameCellHeight;
    private int gameColSpan;
    private int gameRowSpan;

    private DefaultLightRenderer(int windowWidth, int windowHeight, Disposer disposer) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        resolutionChangedDisposer = new DefaultDisposer(disposer);

        DefaultShaderProgram shaderProgram = DefaultShaderProgram.create(disposer);
        shaderProgram.attachShader(loadShader("shaders/LightMap.vertex", ShaderType.VERTEX, disposer));
        shaderProgram.attachShader(loadShader("shaders/LightMap.fragment", ShaderType.FRAGMENT, disposer));

        // variable
        shaderProgram.initialize();
        shaderProgram.initializeMvp(windowWidth, windowHeight);
        shaderProgram.bind();

        // Generate and bind a texture id for the alpha map
        textureId = OpenGL.instance().glGenTextures();
        OpenGLState.bindTexture(textureId);

        // Setup the ST coordinate system
        OpenGL.instance().glTexParameteri(OpenGL.GL_TEXTURE_2D, OpenGL.GL_TEXTURE_WRAP_S, OpenGL.GL_CLAMP_TO_EDGE);
        OpenGL.instance().glTexParameteri(OpenGL.GL_TEXTURE_2D, OpenGL.GL_TEXTURE_WRAP_T, OpenGL.GL_CLAMP_TO_EDGE);

        // Setup what to do when the texture has to be scaled
        OpenGL.instance().glTexParameteri(OpenGL.GL_TEXTURE_2D, OpenGL.GL_TEXTURE_MIN_FILTER, OpenGL.GL_NEAREST);
        OpenGL.instance().glTexParameteri(OpenGL.GL_TEXTURE_2D, OpenGL.GL_TEXTURE_MAG_FILTER, OpenGL.GL_NEAREST);

        renderer = new SurfaceRenderer<>(new float[ShaderParameter.MIN_PARAMETERS + 2]);
        renderer.setColor(0, 0, 0, 1f);
        renderer.setShaderProgram(shaderProgram);
    }

    private static DefaultShader loadShader(String path, ShaderType shaderType, Disposer disposer) {
        try (InputStream stream = DefaultShader.class.getClassLoader().getResourceAsStream(path)) {
            String src = Read.text(stream, StandardCharsets.UTF_8);
            return DefaultShader.create(src, shaderType, disposer);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to load a shader file!"
                    + System.lineSeparator() + ex.getMessage());
        }
    }

    public static DefaultLightRenderer create(int windowWidth, int windowHeight, Disposer disposer) {
        return new DefaultLightRenderer(windowWidth, windowHeight, disposer);
    }

    public static DefaultLightRenderer create(Window window, Disposer disposer) {
        return new DefaultLightRenderer(window.getWidth(), window.getHeight(), disposer);
    }

    public void clearLightMaps() {
        lightMaps.clear();
    }

    @Override
    public void addLightMap(LightMap lightMap) {
        lightMaps.add(lightMap);
    }

    @Override
    public void removeLightMap(LightMap lightMap) {
        lightMaps.remove(lightMap);
    }

    public int getCols() {
        return alphaMapCols;
    }

    public int getRows() {
        return alphaMapRows;
    }

    private void clearLightTexture() {
        for (int i = 0; i < transparencyTexture.limit(); ++i)
            transparencyTexture.put(i, (byte) 0);
        transparencyTexture.clear();
    }

    public void prepare(float gameCellWidth, float gameCellHeight) {
        prepare(gameCellWidth, gameCellHeight, 0, 0);
    }

    public void prepare(float gameCellWidth, float gameCellHeight, int colOffset, int rowOffset) {
        int colSpan = (int) Math.ceil(windowWidth / gameCellWidth);
        int rowSpan = (int) Math.ceil(windowHeight / gameCellHeight);
        prepare(gameCellWidth, gameCellHeight, colOffset, colSpan, rowOffset, rowSpan);
    }

    public void prepare(float gameCellWidth, float gameCellHeight, int colOffset, int colSpan, int rowOffset,
                        int rowSpan) {
        this.colOffset = colOffset;
        this.colSpan = colSpan;
        this.rowOffset = rowOffset;
        this.rowSpan = rowSpan;
        if (this.gameCellWidth != gameCellWidth || this.gameCellHeight != gameCellHeight || this.gameColSpan != colSpan
                || this.gameRowSpan != rowSpan) {
            reallocate(gameCellWidth, gameCellHeight, colSpan, rowSpan);
        } else {
            clearLightTexture();
        }

        position.set(colOffset * gameCellWidth, rowOffset * gameCellHeight);

        for (LightMap lightMap : lightMaps) {
            lightMap.prepare(alphaMapCols, alphaMapRows, colOffset, colSpan, rowOffset, rowSpan);
        }

        prepared = true;
    }

    public void reset() {
        if (!prepared) {
            throw new IllegalStateException("Must call prepare before reset");
        }
        clearLightTexture();
        for (LightMap lightMap : lightMaps) {
            lightMap.prepare(alphaMapCols, alphaMapRows, colOffset, colSpan, rowOffset, rowSpan);
        }
    }

    public void apply() {
        for (LightMap lightMap : lightMaps) {
            lightMap.apply(this);
        }
    }

    private void reallocate(float gameCellWidth, float gameCellHeight, int gameColSpan, int gameRowSpan) {
        final int frequency = 2;

        this.gameCellWidth = gameCellWidth;
        this.gameCellHeight = gameCellHeight;
        this.gameColSpan = gameColSpan;
        this.gameRowSpan = gameRowSpan;

        alphaMapCols = frequency * gameColSpan + 1;
        alphaMapRows = frequency * gameRowSpan + 1;

        // Dispose old resources.
        resolutionChangedDisposer.dispose();

        transparencyTexture = BufferUtils.createByteBuffer((alphaMapCols + 3) * (alphaMapRows + 3));

        renderer.setSurface(Quad.create(new Rectangle(0, 0, alphaMapCols * (gameCellWidth / frequency),
                alphaMapRows * (gameCellHeight / frequency)), resolutionChangedDisposer));
    }

    public void render(float alpha) {
        render(position, alpha);
    }

    private void render(Point position, float alpha) {
        // Bind alpha map texture
        OpenGLState.bindTexture(textureId);

        // Upload texture data
        transparencyTexture.position(transparencyTexture.limit());
        transparencyTexture.flip();
        OpenGL.instance().glPixelStorei(OpenGL.GL_UNPACK_ALIGNMENT, 1);
        OpenGL.instance().glTexImage2D(OpenGL.GL_TEXTURE_2D, 0, OpenGL.GL_R8, alphaMapCols, alphaMapRows, 0,
                OpenGL.GL_RED, OpenGL.GL_UNSIGNED_BYTE, transparencyTexture);

        // Render
        ModelMatrix.instance().translatef(position.getX(), position.getY(), 0);
        renderer.setShaderParameter(PARAMETER_ALPHA_MAP_COLS, alphaMapCols);
        renderer.setShaderParameter(PARAMETER_ALPHA_MAP_ROWS, alphaMapRows);
        renderer.render(alpha);
    }

    public void addLight(int index, float light) {
        int previous = transparencyTexture.get(index) & 0xFF;
        transparencyTexture.put(index, (byte) Math.min(previous + getTransparencyValue(light), 255));
    }

    private float getTransparencyValue(float light) {
        return (light / MAXIMUM_LIGHT) * 255f;
    }
}