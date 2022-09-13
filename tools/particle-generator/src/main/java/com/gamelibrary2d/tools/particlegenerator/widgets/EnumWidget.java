package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.tools.particlegenerator.properties.GenericProperty;

public class EnumWidget<T extends Enum<T>> extends AbstractPointerAwareGameObject {
    private final T[] values;
    private final Label label;
    private final Renderable renderer;
    private final GenericProperty<T> property;

    private T cachedValue;
    private int currentIndex;
    private Rectangle bounds = Rectangle.EMPTY;

    public EnumWidget(Class<T> enumType, Label label, GenericProperty<T> property) {
        this.label = label;
        this.values = enumType.getEnumConstants();
        this.property = property;
        updateLabel();

        renderer = alpha -> {
            updateLabel();
            label.render(alpha);
        };
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
    protected void onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        super.onPointerDown(id, button, x, y, transformedX, transformedY);
        currentIndex = (currentIndex + 1) % values.length;
        cachedValue = values[currentIndex];
        property.set(cachedValue);
        label.setText(cachedValue.toString());
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public Renderable getRenderer() {
        return renderer;
    }
}
