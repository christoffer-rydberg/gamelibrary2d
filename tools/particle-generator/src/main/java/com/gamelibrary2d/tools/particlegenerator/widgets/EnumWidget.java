package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.InputState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.tools.particlegenerator.properties.GenericProperty;

public class EnumWidget<T extends Enum<T>> extends AbstractGameObject implements PointerDownAware {
    private final T[] values;
    private final Label label;
    private final GenericProperty<T> property;
    private final Point pointerPosition = new Point();
    private T cachedValue;
    private int currentIndex;
    private Rectangle bounds = Rectangle.EMPTY;

    public EnumWidget(Class<T> enumType, Label label, GenericProperty<T> property) {
        this.label = label;
        this.values = enumType.getEnumConstants();
        this.property = property;
        updateLabel();
    }

    private void updateLabel() {
        T value = property.get();
        if (cachedValue != value) {
            cachedValue = value;
            currentIndex = -1;
            for (int i = 0; i < values.length; ++i) {
                if (values[i].equals(value)) {
                    currentIndex = i;
                    break;
                }
            }

            label.setText(value.toString());
        }
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
        updateLabel();
        label.render(alpha);
    }

    @Override
    public boolean pointerDown(InputState inputState, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        if (getBounds().contains(pointerPosition)) {
            currentIndex = (currentIndex + 1) % values.length;
            cachedValue = values[currentIndex];
            property.set(cachedValue);
            label.setText(cachedValue.toString());
            return true;
        }

        return false;
    }
}
