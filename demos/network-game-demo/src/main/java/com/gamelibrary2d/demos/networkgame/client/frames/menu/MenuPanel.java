package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.components.AbstractPointerAwareComposedGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.DefaultPanel;
import com.gamelibrary2d.components.containers.NavigationPanel;
import com.gamelibrary2d.components.containers.Panel;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.Button;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.InputField;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.ShadowedLabel;
import com.gamelibrary2d.demos.networkgame.client.options.Options;
import com.gamelibrary2d.demos.networkgame.client.options.RotationMode;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.resources.Textures;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.text.HorizontalTextAlignment;
import com.gamelibrary2d.text.Label;
import com.gamelibrary2d.text.VerticalTextAlignment;

import java.net.Inet4Address;
import java.net.UnknownHostException;

class MenuPanel extends AbstractPointerAwareComposedGameObject<NavigationPanel> {
    private final NavigationPanel navigationPanel;

    MenuPanel(DemoGame game) {
        navigationPanel = new NavigationPanel();

        GameObject hostPanel = createHostPanel(game, navigationPanel);
        GameObject joinPanel = createJoinPanel(game, navigationPanel);
        GameObject networkPanel = createNetworkPanel(navigationPanel, hostPanel, joinPanel);
        GameObject optionsPanel = createOptionsPanel(game, navigationPanel);
        GameObject mainPanel = createMainPanel(game, navigationPanel, networkPanel, optionsPanel);

        navigationPanel.setBounds(hostPanel.getBounds()
                .add(joinPanel.getBounds())
                .add(mainPanel.getBounds()));

        navigationPanel.navigateTo(mainPanel, false);
    }

    @Override
    protected NavigationPanel getComposition() {
        return navigationPanel;
    }

    private static <T extends GameObject> void stack(Panel<T> panel, T obj) {
        panel.stack(obj, Panel.StackOrientation.DOWN, Dimensions.getDefaultVerticalOffset());
    }

    private static Button createButton(String text, ParameterizedAction<Button> onClick) {
        ContentRenderer background = new SurfaceRenderer<>(
                Surfaces.button(),
                Textures.button());

        Label label = new Label(Fonts.button(), text);
        label.setColor(Colors.BUTTON_FONT);
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);

        return new Button(new ShadowedLabel(label, Colors.BUTTON_FONT_SHADOW), background, background.getBounds(), onClick);
    }

    private static InputField createInputField(String text) {
        ContentRenderer background = new SurfaceRenderer<>(
                Surfaces.inputField(),
                Textures.inputField());

        Label label = new Label(Fonts.inputField(), text);
        label.setColor(Colors.INPUT_FIELD_FONT);
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);

        return new InputField(new ShadowedLabel(label, Colors.INPUT_FIELD_FONT_SHADOW), background, background.getBounds());
    }

    private static void centerPanel(Panel<GameObject> panel) {
        for (GameObject item : panel.getItems()) {
            item.getPosition().subtract(panel.getBounds().getCenterX(), panel.getBounds().getCenterY());
        }

        panel.recalculateBounds();
    }

    private static String getRotationModeText(DemoGame game) {
        RotationMode rotationMode = game.getOptions().getRotationMode();
        switch (rotationMode) {
            case LEFT_OR_RIGHT:
                return "Rotation mode: Left/right";
            case TOWARD_DIRECTION:
                return "Rotation mode: Direction";
            default:
                throw new IllegalStateException("Unexpected value: " + rotationMode);
        }
    }

    private static void toggleRotationMode(DemoGame game, Button button) {
        Options options = game.getOptions();
        RotationMode rotationMode = options.getRotationMode();
        switch (rotationMode) {
            case LEFT_OR_RIGHT:
                options.setRotationMode(RotationMode.TOWARD_DIRECTION);
                break;
            case TOWARD_DIRECTION:
                options.setRotationMode(RotationMode.LEFT_OR_RIGHT);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + rotationMode);
        }

        button.getLabel().getLabel().setText(getRotationModeText(game));
    }

    private static GameObject createOptionsPanel(DemoGame game, NavigationPanel navigationPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();
        stack(panel, createButton(getRotationModeText(game), b -> toggleRotationMode(game, b)));
        stack(panel, createButton("Go back", b -> navigationPanel.goBack()));
        centerPanel(panel);
        return panel;
    }

    private static GameObject createNetworkPanel(NavigationPanel navigationPanel, GameObject hostPanel, GameObject joinPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();
        stack(panel, createButton("Join Server", b -> navigationPanel.navigateTo(joinPanel, true)));
        stack(panel, createButton("Host Server", b -> navigationPanel.navigateTo(hostPanel, true)));
        stack(panel, createButton("Go back", b -> navigationPanel.goBack()));
        centerPanel(panel);
        return panel;
    }

    private static GameObject createMainPanel(DemoGame game, NavigationPanel navigationPanel, GameObject networkPanel, GameObject optionsPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();
        stack(panel, createButton("New Game", b -> game.startLocalGame()));
        stack(panel, createButton("Multiplayer", b -> navigationPanel.navigateTo(networkPanel, true)));
        stack(panel, createButton("Options", b -> navigationPanel.navigateTo(optionsPanel, true)));
        stack(panel, createButton("Exit", b -> game.exit()));
        centerPanel(panel);
        return panel;
    }

    private static GameObject createHostPanel(DemoGame game, NavigationPanel navigationPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();

        String ip;
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "localhost";
        }

        InputField ipField = createInputField(ip);
        InputField tcpField = createInputField("4444");
        stack(panel, ipField);
        stack(panel, tcpField);
        stack(panel, createButton("Start game", b ->
                game.hostNetworkGame(ipField.getStringValue(), tcpField.getIntValue())));
        stack(panel, createButton("Go back", b -> navigationPanel.goBack()));
        centerPanel(panel);
        return panel;
    }

    private static GameObject createJoinPanel(DemoGame game, NavigationPanel navigationPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();

        InputField ipField = createInputField("192.168.0.32");
        InputField tcpField = createInputField("4444");

        Button startButton = createButton("Start game",
                b -> game.joinNetworkGame(ipField.getStringValue(), tcpField.getIntValue()));

        Button backButton = createButton("Go back", b -> navigationPanel.goBack());

        stack(panel, ipField);
        stack(panel, tcpField);
        stack(panel, startButton);
        stack(panel, backButton);
        centerPanel(panel);

        return panel;
    }
}
