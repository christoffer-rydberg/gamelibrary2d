package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.Label;

public class ToggleButton extends AbstractGameObject implements PointerDownAware, PointerUpAware {
    private final Label label;
    private final Point pointerPosition = new Point();
    private boolean toggled;
    private float defaultR;
    private float defaultG;
    private float defaultB;
    private float defaultA;
    private Rectangle bounds = Rectangle.EMPTY;

    private int pointerId = -1;
    private int pointerButton = -1;

    public ToggleButton(Font font, String text) {
        this.label = new Label(font, text);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        if (toggled) {
            defaultR = label.getShaderParameter(ShaderParameter.COLOR_R);
            defaultG = label.getShaderParameter(ShaderParameter.COLOR_G);
            defaultB = label.getShaderParameter(ShaderParameter.COLOR_B);
            defaultA = label.getShaderParameter(ShaderParameter.ALPHA);
            label.setColor(Color.GREEN);
        } else {
            label.setColor(defaultR, defaultG, defaultB, defaultA);
        }
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    protected void onRender(float alpha) {
        label.render(alpha);
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        if (getBounds().contains(pointerPosition)) {
            pointerId = id;
            pointerButton = button;
            return true;
        }

        return false;
    }

    @Override
    public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            setToggled(!isToggled());
        }
    }
}