package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.renderers.LineRenderer;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.properties.BooleanProperty;

public class Checkbox extends AbstractGameObject implements PointerDownAware {
    private final BooleanProperty checked;
    private final Action onChecked;
    private final Action onUnchecked;
    private final CheckboxRenderer renderer;
    private final Point pointerPosition = new Point();
    private boolean cachedValue;

    public Checkbox(Box box, LineRenderer lineRenderer, Font font, BooleanProperty checked) {
        this.checked = checked;
        this.renderer = new CheckboxRenderer(box, lineRenderer, font);
        this.onChecked = null;
        this.onUnchecked = null;
    }

    public Checkbox(Box box, LineRenderer lineRenderer, Font font, BooleanProperty checked, Action onChecked, Action onUnchecked) {
        this.checked = checked;
        this.renderer = new CheckboxRenderer(box, lineRenderer, font);
        this.onChecked = onChecked;
        this.onUnchecked = onUnchecked;
    }

    private void updateCheckbox() {
        boolean value = checked.get();
        if (cachedValue != value) {
            cachedValue = value;
            if (value) {
                renderer.check();
                if (onChecked != null) {
                    onChecked.perform();
                }
            } else {
                renderer.uncheck();
                if (onUnchecked != null) {
                    onUnchecked.perform();
                }
            }
        }
    }

    @Override
    public void onRender(float alpha) {
        updateCheckbox();
        renderer.render(alpha);
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        if (getBounds().contains(pointerPosition)) {
            toggle();
            return true;
        }

        return false;
    }

    public void toggle() {
        checked.set(!checked.get());
        updateCheckbox();
    }

    @Override
    public Rectangle getBounds() {
        return renderer.getBounds();
    }

    private static class CheckboxRenderer implements Renderable, Bounded {
        private final Box box;
        private final LineRenderer renderer;
        private final Label label;

        CheckboxRenderer(Box box, LineRenderer renderer, Font font) {
            this.box = box;
            this.renderer = renderer;
            label = new Label(font);
            label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
        }

        public void check() {
            label.setText("V");
            label.setColor(Color.GREEN);
        }

        public void uncheck() {
            label.setText("");
        }

        @Override
        public void render(float alpha) {
            box.render(renderer, alpha);
            float centerX = box.getBounds().getCenterX();
            float centerY = box.getBounds().getCenterY();
            ModelMatrix.instance().pushMatrix();
            ModelMatrix.instance().translatef(centerX, centerY, 0f);
            label.render(alpha);
            ModelMatrix.instance().popMatrix();
        }

        @Override
        public Rectangle getBounds() {
            return box.getBounds();
        }
    }
}
