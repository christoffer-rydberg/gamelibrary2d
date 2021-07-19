package com.gamelibrary2d.demos.networkgame.client.frames.menu;

import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.containers.DefaultPanel;
import com.gamelibrary2d.components.containers.NavigationPanel;
import com.gamelibrary2d.components.containers.Panel;
import com.gamelibrary2d.components.objects.AbstractPointerAwareComposedGameObject;
import com.gamelibrary2d.components.objects.GameObject;
import com.gamelibrary2d.renderers.Label;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.Button;
import com.gamelibrary2d.demos.networkgame.client.objects.widgets.InputField;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.resources.Surfaces;
import com.gamelibrary2d.demos.networkgame.client.resources.Textures;
import com.gamelibrary2d.demos.networkgame.client.settings.Colors;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.renderers.ContentRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.HorizontalTextAlignment;
import com.gamelibrary2d.resources.StackOrientation;
import com.gamelibrary2d.resources.VerticalTextAlignment;

class MenuPanel extends AbstractPointerAwareComposedGameObject<NavigationPanel> {
    MenuPanel(DemoGame game) {
        NavigationPanel navigationPanel = new NavigationPanel();

        GameObject hostPanel = createHostPanel(game, navigationPanel);
        GameObject joinPanel = createJoinPanel(game, navigationPanel);
        GameObject mainPanel = createMainPanel(game, navigationPanel, hostPanel, joinPanel);

        navigationPanel.setBounds(hostPanel.getBounds()
                .add(joinPanel.getBounds())
                .add(mainPanel.getBounds()));

        navigationPanel.navigateTo(mainPanel, false);

        setContent(navigationPanel);
    }

    private static <T extends GameObject> void stack(Panel<T> panel, T obj) {
        panel.stack(obj, StackOrientation.DOWN, Dimensions.getDefaultOffset());
    }

    private static Button createButton(String text, Action onClick) {
        ContentRenderer background = new SurfaceRenderer<>(
                Surfaces.button(),
                Textures.button());

        background.setColor(Colors.BUTTON_COLOR);

        Label label = new Label(Fonts.button(), text);
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);

        return new Button(label, background, background.getBounds(), onClick);
    }

    private static InputField createInputField(String text) {
        ContentRenderer background = new SurfaceRenderer<>(
                Surfaces.inputField(),
                Textures.inputField());

        background.setColor(Colors.INPUT_FIELD_COLOR);

        Label textField = new Label(Fonts.inputField(), text);
        textField.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);

        Label label = new Label(Fonts.button(), text);
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);

        return new InputField(textField, background, background.getBounds());
    }

    private static GameObject createMainPanel(DemoGame game, NavigationPanel navigationPanel, GameObject hostPanel, GameObject joinPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();

        stack(panel, createButton("Local", game::startLocalGame));
        stack(panel, createButton("Join", () -> navigationPanel.navigateTo(joinPanel, true)));
        stack(panel, createButton("Host", () -> navigationPanel.navigateTo(hostPanel, true)));
        stack(panel, createButton("Exit", game::exit));

        return panel;
    }

    private static GameObject createHostPanel(DemoGame game, NavigationPanel navigationPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();

        InputField tcpField = createInputField("4444");
        InputField udpField = createInputField("4444");

        stack(panel, tcpField);
        stack(panel, udpField);
        stack(panel, createButton("Start game", () ->
                game.hostNetworkGame(tcpField.getIntValue(), udpField.getIntValue())));
        stack(panel, createButton("Go back", navigationPanel::goBack));

        return panel;
    }

    private static GameObject createJoinPanel(DemoGame game, NavigationPanel navigationPanel) {
        Panel<GameObject> panel = new DefaultPanel<>();

        InputField ipField = createInputField("localhost");
        InputField tcpField = createInputField("4444");
        InputField udpField = createInputField("4444");

        Button startButton = createButton("Start game",
                () -> game.joinNetworkGame(ipField.getStringValue(), tcpField.getIntValue(), udpField.getIntValue()));

        Button backButton = createButton("Go back", navigationPanel::goBack);

        stack(panel, ipField);
        stack(panel, tcpField);
        stack(panel, udpField);
        stack(panel, startButton);
        stack(panel, backButton);

        return panel;
    }
}
