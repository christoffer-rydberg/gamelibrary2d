package com.gamelibrary2d.demos.splitscreen;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.random.RandomGenerator;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.DefaultGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.DefaultLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.containers.DefaultLayerGameObject;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;
import com.gamelibrary2d.splitscreen.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DemoFrame extends AbstractFrame implements KeyDownAware {
    private final static Rectangle GAME_BOUNDS = new Rectangle(0, 0, 4000, 4000);
    private final static float SPLIT_LAYOUT_MARGIN = 1f;
    private final static Color SPLIT_COLOR = Color.WHITE;
    private final static Color BACKGROUND_COLOR = Color.BLACK;
    private final static int INITIAL_SPACECRAFT_COUNT = 5;
    private final DefaultLayerGameObject<Renderable> spacecraftLayer;
    private final Game game;

    private GameObject view;
    private Quad spaceCraftQuad;
    private Texture spaceCraftTexture;
    private List<SpaceCraft> spaceCrafts;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
        spacecraftLayer = new DefaultLayerGameObject<>();
        setBackgroundColor(SPLIT_COLOR);
    }

    private SpaceCraft createSpaceCraft(Quad quad, Texture texture) {
        RandomGenerator random = RandomInstance.get();
        ContentRenderer renderer = new SurfaceRenderer<>(quad, texture);
        renderer.setColor(
                random.nextFloat() * 0.5f + 0.5f,
                random.nextFloat() * 0.5f + 0.5f,
                random.nextFloat() * 0.5f + 0.5f);

        SpaceCraft spaceCraft = new SpaceCraft(GAME_BOUNDS, renderer);
        spaceCraft.setPosition(
                GAME_BOUNDS.getLowerX() + random.nextFloat() * GAME_BOUNDS.getWidth(),
                GAME_BOUNDS.getLowerY() + random.nextFloat() * GAME_BOUNDS.getHeight());

        return spaceCraft;
    }

    private Renderable createStars(int count) {
        RandomGenerator random = RandomInstance.get();
        float[] positions = new float[count * 2];
        for (int i = 0; i < count; ++i) {
            float x = GAME_BOUNDS.getLowerX() + random.nextFloat() * GAME_BOUNDS.getWidth();
            float y = GAME_BOUNDS.getLowerY() + random.nextFloat() * GAME_BOUNDS.getHeight();
            int index = i * 2;
            positions[index] = x;
            positions[index + 1] = y;
        }

        PositionBuffer starPositions = PositionBuffer.create(positions, this);
        QuadsRenderer starsRenderer = new QuadsRenderer(Rectangle.create(8f, 8f));
        starsRenderer.setShape(QuadShape.RADIAL_GRADIENT);
        starsRenderer.setColor(Color.LIGHT_YELLOW);

        return a -> starsRenderer.render(a, starPositions, 0, starPositions.getCapacity());
    }

    private void prepareView(SpaceCraft spaceCraft, Rectangle viewArea) {
        float x = Math.min(
                GAME_BOUNDS.getWidth() - viewArea.getWidth() / 2,
                Math.max(spaceCraft.getPosition().getX(), viewArea.getWidth() / 2));
        float y = Math.min(
                GAME_BOUNDS.getHeight() - viewArea.getHeight() / 2,
                Math.max(spaceCraft.getPosition().getY(), viewArea.getHeight() / 2));
        spacecraftLayer.setPosition(viewArea.getWidth() / 2 - x, viewArea.getHeight() / 2 - y);
    }

    private SplitLayout createSplitLayoutHelper(List<SpaceCraft> spaceCrafts, SplitOrientation orientation) {
        int size = spaceCrafts.size();

        if (size == 1) {
            SpaceCraft spaceCraft = spaceCrafts.get(0);
            spacecraftLayer.setPosition(spaceCraft.getPosition());
            return new SplitLayoutLeaf<>(spacecraftLayer, this::prepareView, spaceCraft, this);
        }

        SplitOrientation flippedOrientation = orientation == SplitOrientation.HORIZONTAL
                ? SplitOrientation.VERTICAL
                : SplitOrientation.HORIZONTAL;

        SplitLayout layout1 = createSplitLayoutHelper(spaceCrafts.subList(0, size / 2), flippedOrientation);
        SplitLayout layout2 = createSplitLayoutHelper(spaceCrafts.subList(size / 2, size), flippedOrientation);

        SplitLayoutBranch layout = new SplitLayoutBranch(SPLIT_LAYOUT_MARGIN, orientation);
        layout.getLayouts().add(layout1);
        layout.getLayouts().add(layout2);

        return layout;
    }

    private SplitLayout createSplitLayout(List<SpaceCraft> spaceCrafts) {
        return createSplitLayoutHelper(spaceCrafts, SplitOrientation.HORIZONTAL);
    }

    private GameObject createBackgroundColor() {
        Surface quad = Quad.create(GAME_BOUNDS, this);
        ContentRenderer renderer = new SurfaceRenderer<>(quad);
        renderer.setColor(
                BACKGROUND_COLOR.getR(),
                BACKGROUND_COLOR.getG(),
                BACKGROUND_COLOR.getB(),
                BACKGROUND_COLOR.getA());

        return new DefaultGameObject<>(renderer);
    }

    private Renderable createBackground() {
        Layer<Renderable> backgroundLayer = new DefaultLayer<>();
        backgroundLayer.setAutoClearing(false);
        backgroundLayer.add(createBackgroundColor());
        backgroundLayer.add(createStars(Math.round(GAME_BOUNDS.getArea() * 0.0001f)));
        return backgroundLayer;
    }

    private void refreshSplitLayout(List<SpaceCraft> spaceCrafts) {
        Window window = game.getWindow();
        if (view != null) {
            remove(view);
        }

        if (spaceCrafts.size() > 0) {
            Rectangle viewArea = new Rectangle(0, 0, window.getWidth(), window.getHeight());
            SplitLayer<GameObject> splitLayer = new SplitLayer<>(createSplitLayout(spaceCrafts), viewArea);
            splitLayer.setTarget(spacecraftLayer);
            add(splitLayer);
            this.view = splitLayer;
        } else {
            add(spacecraftLayer);
            this.view = spacecraftLayer;
        }
    }

    private void addSpaceCraft() {
        SpaceCraft spaceCraft = createSpaceCraft(spaceCraftQuad, spaceCraftTexture);
        spaceCrafts.add(spaceCraft);
        spacecraftLayer.add(spaceCraft);
    }

    private void removeSpaceCraft() {
        if (spaceCrafts.size() > 0) {
            SpaceCraft spaceCraft = spaceCrafts.remove(spaceCrafts.size() - 1);
            spacecraftLayer.remove(spaceCraft);
        }
    }

    @Override
    protected void onInitialize(FrameInitializer initializer) {
        try {
            spacecraftLayer.add(0, createBackground());

            spaceCraftQuad = Quad.create(Rectangle.create(64, 64), this);

            spaceCraftTexture = DefaultTexture.create(
                    getClass().getResource("/spacecraft.png"),
                    this);

            spaceCrafts = new ArrayList<>();
            for (int i = 0; i < INITIAL_SPACECRAFT_COUNT; ++i) {
                addSpaceCraft();
            }

            refreshSplitLayout(spaceCrafts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onInitializationFailed(Throwable error) {

    }

    @Override
    protected void onInitializationSuccessful(FrameInitializationContext context) {

    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (key == Keyboard.instance().keyUp()) {
            addSpaceCraft();
            refreshSplitLayout(spaceCrafts);
        } else if (key == Keyboard.instance().keyDown()) {
            removeSpaceCraft();
            refreshSplitLayout(spaceCrafts);
        }
    }
}
