package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.Label;

public class ToggleButton extends AbstractPointerAwareGameObject {
    private final Label label;

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

    @Override
    protected boolean onPointerDown(int id, int button, float transformedX, float transformedY) {
        pointerId = id;
        pointerButton = button;
        return true;
    }

    @Override
    protected void onPointerUp(int id, int button, float transformedX, float transformedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            setToggled(!isToggled());
        }
    }

    @Override
    protected boolean isTrackingPointerPositions() {
        return false;
    }

    @Override
    protected void onPointerEntered(int id) {

    }

    @Override
    protected void onPointerLeft(int id) {

    }

    @Override
    protected boolean onPointerMove(int id, float transformedX, float transformedY) {
        return false;
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
}