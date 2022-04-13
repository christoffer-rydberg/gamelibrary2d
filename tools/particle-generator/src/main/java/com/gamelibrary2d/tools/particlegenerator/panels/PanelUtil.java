package com.gamelibrary2d.tools.particlegenerator.panels;

import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.containers.Panel;

public class PanelUtil {

    public final static float DEFAULT_STACK_MARGIN = 3f;

    public static <T extends GameObject> void stack(Panel<T> panel, T element) {
        panel.stack(element, Panel.StackOrientation.DOWN, DEFAULT_STACK_MARGIN);
    }

    public static <T extends GameObject> void stack(Panel<T> panel, T element, float offset) {
        panel.stack(element, Panel.StackOrientation.DOWN, offset);
    }
}
