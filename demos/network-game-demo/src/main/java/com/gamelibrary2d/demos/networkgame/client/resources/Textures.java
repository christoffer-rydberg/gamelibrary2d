package com.gamelibrary2d.demos.networkgame.client.resources;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Texture;

public final class Textures {
    private static Texture button;
    private static Texture inputField;

    private Textures() {

    }

    public static void create(Disposer disposer) {
        button = createQuadStackTexture(
                Dimensions.getButtonSize(),
                Colors.BUTTON_BACKGROUND,
                Colors.BUTTON_FOREGROUND,
                10,
                disposer);

        inputField = createQuadStackTexture(
                Dimensions.getInputFieldSize(),
                Colors.INPUT_FIELD_BACKGROUND,
                Colors.INPUT_FIELD_FOREGROUND,
                10,
                disposer);
    }

    public static Texture button() {
        return button;
    }

    public static Texture inputField() {
        return inputField;
    }

    public static Texture createQuadStackTexture(Rectangle bounds, Color bottom, Color top, int depth, Disposer disposer) {
        Color deltaColor = top.subtract(bottom);

        Renderable[] layers = new Renderable[depth];
        for (int i = 0; i < depth; ++i) {
            float interpolation = i / (float) depth;
            layers[i] = createQuadRenderer(
                    bounds.pad(i * -1),
                    bottom.add(deltaColor.multiply(interpolation)),
                    disposer);
        }

        Renderable r = a -> {
            for (Renderable layer : layers) {
                layer.render(1f);
            }
        };

        return DefaultTexture.create(r, 1f, bounds, disposer);
    }

    public static Renderable createQuadRenderer(Rectangle bounds, Color color, Disposer disposer) {
        ContentRenderer renderer = new SurfaceRenderer<>(Quad.create(bounds, disposer));
        renderer.setColor(color);
        return renderer;
    }
}
