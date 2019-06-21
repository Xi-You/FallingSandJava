package com.gdx.cellular;


import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class EffectColors {

    private static List<Color >fireColors = new ArrayList<>();

    public static Color TITANIUM_COLOR = new Color(201,201,201,1);

    static {
        fireColors.add(Color.RED);
        fireColors.add(Color.YELLOW);
        fireColors.add(Color.YELLOW);
        fireColors.add(Color.ORANGE);
        fireColors.add(Color.ORANGE);
        fireColors.add(Color.ORANGE);
    }

    public static Color getRandomFireColor() {
        return fireColors.get((int) Math.floor(Math.random() * fireColors.size()));
    }
}
