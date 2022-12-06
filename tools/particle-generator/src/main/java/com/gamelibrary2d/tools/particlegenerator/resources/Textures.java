package com.gamelibrary2d.tools.particlegenerator.resources;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Texture;

import java.io.IOException;

public class Textures {
    private static Texture propertyBaseLine;
    private static Texture sliderHandle;

    public static void create(Disposer disposer) throws IOException {
        propertyBaseLine = createQuadStackTexture(
                Bounds.PROPERTY_BASE_LINE,
                Color.WHITE.divide(2),
                Color.WHITE,
                2,
                disposer);

        sliderHandle = DefaultTexture.create(
                Textures.class.getResource("/Images/sliderHandle.png"),
                disposer
        );
    }

    public static Texture propertyBaseLine() {
        return propertyBaseLine;
    }

    public static Texture sliderHandle() {
        return sliderHandle;
    }

    private static Texture createQuadStackTexture(Rectangle bounds, Color bottom, Color top, int depth, Disposer disposer) {
        Color deltaColor = top.subtract(bottom);

        Renderable[] layers = new Renderable[depth];
        for (int i = 0; i < depth; ++i) {
            float interpolation = (i + 1) / (float) depth;
            layers[i] = createQuadRenderer(
                    bounds.pad(0, -i),
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

    private static Renderable createQuadRenderer(Rectangle bounds, Color color, Disposer disposer) {
        ContentRenderer renderer = new SurfaceRenderer<>(Quad.create(bounds, disposer));
        renderer.setColor(color);
        return renderer;
    }
}
