package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.renderers.Label;
import com.gamelibrary2d.resources.Font;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;

public class FloatPropertyTextField extends Label {
    private static final Color VALID = Color.WHITE;
    private static final Color INVALID = Color.RED;

    private final FloatProperty property;

    private float cachedValue;

    public FloatPropertyTextField(Font font, FloatProperty property) {
        super(font, toString(property.get()));
        this.property = property;
        cachedValue = property.get();
        setColor(VALID);
        addTextChangedListener(this::onTextChanged);
    }

    public static String toString(float value) {
        return value == (int) value
                ? Integer.toString((int) value)
                : Float.toString(value);
    }

    private void updateLabel() {
        float value = property.get();
        if (cachedValue != value) {
            cachedValue = value;
            setText(toString(value));
        }
    }

    private void onTextChanged(String before, String after) {
        try {
            property.set(Float.parseFloat(after));
            setColor(VALID);
        } catch (Exception e) {
            setColor(INVALID);
        }
    }

    @Override
    public void render(float alpha) {
        updateLabel();
        super.render(alpha);
    }
}