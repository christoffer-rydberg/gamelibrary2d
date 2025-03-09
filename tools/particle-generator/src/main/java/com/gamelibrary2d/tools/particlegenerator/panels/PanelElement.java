package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.DefaultGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.AbstractPanel;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.LineRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;
import com.gamelibrary2d.tools.particlegenerator.ParticleGenerator;
import com.gamelibrary2d.tools.particlegenerator.properties.BooleanProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.FloatProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.GenericProperty;
import com.gamelibrary2d.tools.particlegenerator.properties.IntegerProperty;
import com.gamelibrary2d.tools.particlegenerator.resources.Bounds;
import com.gamelibrary2d.tools.particlegenerator.resources.Fonts;
import com.gamelibrary2d.tools.particlegenerator.resources.Surfaces;
import com.gamelibrary2d.tools.particlegenerator.resources.Textures;
import com.gamelibrary2d.tools.particlegenerator.widgets.*;

public class PanelElement {
    private static final Color BASE_LINE_COLOR = Color.LIGHT_BLUE;
    private static final Color ENUM_BASE_LINE_COLOR = Color.LAVENDER;
    private static final Color ENUM_FONT_COLOR = Color.LIGHT_BLUE;

    private static Label createLabel(
            String text,
            Font font,
            Color color,
            HorizontalTextAlignment horizontalAlignment,
            VerticalTextAlignment verticalAlignment) {
        Label label = new Label(font, text);
        label.setColor(color);
        label.setAlignment(horizontalAlignment, verticalAlignment);
        return label;
    }

    private static DefaultGameObject<Label> createLabelObject(
            String text,
            Font font,
            Color color,
            HorizontalTextAlignment horizontalAlignment,
            VerticalTextAlignment verticalAlignment) {
        return new DefaultGameObject<>(createLabel(
                text, font, color, horizontalAlignment, verticalAlignment
        ));
    }

    private static DefaultGameObject<Label> createParameterLabel(String parameterName) {
        DefaultGameObject<Label> label = createLabelObject(
                parameterName + ":",
                Fonts.getDefaultFont(),
                Color.SAND,
                HorizontalTextAlignment.RIGHT,
                VerticalTextAlignment.BASE_LINE
        );

        label.setPosition(-10, 0);
        return label;
    }

    public static class Boolean extends AbstractPanel<GameObject> {
        public Boolean(String parameterName, BooleanProperty parameter) {
            DefaultGameObject<Label> label = createParameterLabel(parameterName);

            LineRenderer lineRenderer = new LineRenderer();
            lineRenderer.setColor(Color.SILVER);

            Checkbox checkBox = new Checkbox(
                    Surfaces.defaultCheckbox(),
                    lineRenderer,
                    Fonts.getLargeFont(),
                    parameter);

            checkBox.setPosition(
                    Surfaces.propertyBaseLine().getBounds().getWidth() / 2f - checkBox.getBounds().getCenterX(),
                    -Surfaces.defaultCheckbox().getBounds().getLowerY() - 2f);

            add(label);
            add(checkBox);
        }
    }

    public static class Float extends AbstractPanel<GameObject> {

        public Float(String parameterName, FloatProperty parameter) {
            this(parameterName, parameter, null);
        }

        public Float(String parameterName, FloatProperty parameter, FloatProperty variance) {
            DefaultGameObject<Label> label = createParameterLabel(parameterName);

            GameObject parameterWidget = createPropertyWidget(parameter, BASE_LINE_COLOR);
            parameterWidget.setPosition(-parameterWidget.getBounds().getLowerX(), 1f);

            add(label);
            add(parameterWidget);

            if (variance != null) {
                GameObject varianceWidget = createPropertyWidget(variance, BASE_LINE_COLOR);
                varianceWidget.setPosition(120, 1f);

                DefaultGameObject<Label> plusLabel = createLabelObject(
                        "+",
                        Fonts.getDefaultFont(),
                        Color.SAND,
                        HorizontalTextAlignment.CENTER,
                        VerticalTextAlignment.CENTER);

                plusLabel.setBounds(Rectangle.EMPTY);

                plusLabel.setPosition(
                        varianceWidget.getPosition().getX() + varianceWidget.getBounds().getLowerX() - 8,
                        varianceWidget.getPosition().getY() + 3);

                DefaultGameObject<Label> minusLabel = createLabelObject(
                        "-",
                        Fonts.getDefaultFont(),
                        Color.SAND,
                        HorizontalTextAlignment.CENTER,
                        VerticalTextAlignment.CENTER);

                minusLabel.setBounds(Rectangle.EMPTY);

                minusLabel.setPosition(
                        varianceWidget.getPosition().getX() + varianceWidget.getBounds().getLowerX() - 8,
                        varianceWidget.getPosition().getY() - 3);

                add(plusLabel);
                add(minusLabel);
                add(varianceWidget);
            }
        }

        private GameObject createPropertyWidget(FloatProperty property, Color backgroundColor) {
            ContentRenderer background = new SurfaceRenderer<>(
                    Surfaces.propertyBaseLine(),
                    Textures.propertyBaseLine()
            );

            float backgroundAlpha = 0.5f;
            background.setColor(backgroundColor, backgroundAlpha);

            FloatPropertyTextField label = new FloatPropertyTextField(Fonts.getDefaultFont(), property);
            label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.BASE_LINE);

            TextField propertyWidget = new TextField(label);
            propertyWidget.setBackground(background);
            propertyWidget.addFocusChangedListener(focused ->
                    background.setColor(focused ? Color.GOLD : backgroundColor, backgroundAlpha));
            propertyWidget.setBounds(Bounds.PROPERTY_BASE_LINE);

            return propertyWidget;
        }
    }

    public static class Integer extends AbstractPanel<GameObject> {
        public Integer(String parameterName, IntegerProperty parameter) {
            this(parameterName, parameter, null);
        }

        public Integer(String parameterName, IntegerProperty parameter, IntegerProperty variance) {
            GameObject label = createParameterLabel(parameterName);

            GameObject parameterWidget = createPropertyWidget(parameter, BASE_LINE_COLOR);
            parameterWidget.setPosition(-parameterWidget.getBounds().getLowerX(), 1f);

            add(label);
            add(parameterWidget);

            if (variance != null) {
                GameObject varianceWidget = createPropertyWidget(variance, BASE_LINE_COLOR);
                varianceWidget.setPosition(120, 1f);

                GameObject plusLabel = createLabelObject(
                        "+",
                        Fonts.getDefaultFont(),
                        Color.SAND,
                        HorizontalTextAlignment.CENTER,
                        VerticalTextAlignment.BASE_LINE);

                plusLabel.setPosition(
                        varianceWidget.getPosition().getX() + varianceWidget.getBounds().getLowerX() - 8,
                        varianceWidget.getPosition().getY() + 3);

                GameObject minusLabel = createLabelObject(
                        "-",
                        Fonts.getDefaultFont(),
                        Color.SAND,
                        HorizontalTextAlignment.CENTER,
                        VerticalTextAlignment.BASE_LINE);

                minusLabel.setPosition(
                        varianceWidget.getPosition().getX() + varianceWidget.getBounds().getLowerX() - 8,
                        varianceWidget.getPosition().getY() - 3);

                add(plusLabel);
                add(minusLabel);
                add(varianceWidget);
            }
        }

        private GameObject createPropertyWidget(IntegerProperty property, Color backgroundColor) {
            ContentRenderer background = new SurfaceRenderer<>(
                    Surfaces.propertyBaseLine(),
                    Textures.propertyBaseLine()
            );

            background.setColor(backgroundColor);
            background.setShaderParameter(ShaderParameter.ALPHA, 0.5f);

            Label label = new IntegerPropertyTextField(Fonts.getDefaultFont(), property);
            label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.BASE_LINE);

            TextField propertyWidget = new TextField(label);
            propertyWidget.setBackground(background);
            propertyWidget.addFocusChangedListener(focused ->
                    background.setColor(focused ? Color.GOLD : backgroundColor));
            propertyWidget.setBounds(Bounds.PROPERTY_BASE_LINE.pad(0, 0, 0, 15f));

            return propertyWidget;
        }
    }

    public static class Optional extends AbstractPanel<GameObject> {
        private final GameObject content;

        public Optional(GameObject content, BooleanProperty isEnabled) {
            this.content = content;

            Rectangle bounds = content.getBounds().pad(2f, 2f, 40f, 2f);
            Box box = Box.create(bounds, ParticleGenerator.GLOBAL_DISPOSER);
            LineRenderer lineRenderer = new LineRenderer();

            DefaultGameObject<Renderable> boundingBox = new DefaultGameObject<>(alpha -> box.render(lineRenderer, alpha));
            boundingBox.setBounds(bounds);

            LineRenderer checkBoxRenderer = new LineRenderer();
            checkBoxRenderer.setColor(Color.SILVER);

            Checkbox checkBox = new Checkbox(
                    Surfaces.cornerCheckbox(),
                    checkBoxRenderer,
                    Fonts.getDefaultFont(),
                    isEnabled,
                    this::onEnabled,
                    this::onDisabled);

            checkBox.setPosition(
                    bounds.getUpperX() - checkBox.getBounds().getUpperX() - 6f,
                    bounds.getUpperY() - checkBox.getBounds().getUpperY() - 6f);

            add(content);
            add(boundingBox);
            add(checkBox);

            if (isEnabled.get()) {
                onEnabled();
            } else {
                onDisabled();
            }
        }

        private void onEnabled() {
            content.setOpacity(1f);
        }

        private void onDisabled() {
            content.setOpacity(0.5f);
        }
    }

    public static class Enum<T extends java.lang.Enum<T>> extends AbstractPanel<GameObject> {

        public Enum(String parameterName, Class<T> enumType, GenericProperty<T> property) {
            GameObject label = createParameterLabel(parameterName);

            ContentRenderer background = new SurfaceRenderer<>(
                    Surfaces.propertyBaseLine(),
                    Textures.propertyBaseLine()
            );

            background.setColor(ENUM_BASE_LINE_COLOR);
            background.setShaderParameter(ShaderParameter.ALPHA, 0.5f);

            DefaultGameObject<ContentRenderer> backgroundObj = new DefaultGameObject<>(background);
            backgroundObj.setPosition(-backgroundObj.getBounds().getLowerX(), 0f);
            backgroundObj.setBounds(Bounds.PROPERTY_BASE_LINE.pad(0, 0, 0, 15f));

            Label enumLabel = createLabel(
                    property.get().toString(),
                    Fonts.getSmallFont(),
                    ENUM_FONT_COLOR,
                    HorizontalTextAlignment.CENTER,
                    VerticalTextAlignment.BASE_LINE
            );

            float backgroundWidth = backgroundObj.getBounds().getWidth();

            EnumWidget<T> widget = new EnumWidget<>(enumType, enumLabel, property);
            widget.setPosition(backgroundWidth / 2, 0);
            widget.setBounds(new Rectangle(
                    -backgroundWidth / 2,
                    backgroundObj.getBounds().getLowerY(),
                    backgroundWidth / 2,
                    backgroundObj.getBounds().getUpperY()));

            add(label);
            add(backgroundObj);
            add(widget);
        }
    }

    public static class CustomElement extends AbstractPanel<GameObject> {

        public CustomElement(String parameterName, GameObject element) {
            GameObject label = createParameterLabel(parameterName);
            add(label);
            add(element);
        }
    }
}
