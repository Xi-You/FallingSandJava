package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.badlogic.gdx.Gdx;
import com.gdx.cellular.util.MaterialMap;

public class ColorConstants {

    private static final Map<String, Color> colorCache = new ConcurrentHashMap<>();

    private static final Map<String, MaterialMap> materialsMap = new HashMap<>();
    // Movable Solids
    public static final Color SAND_1 = new Color(255/255f, 255/255f, 0/255f, 1);
    public static final Color SAND_2 = new Color(178/255f, 201/255f, 6/255f, 1);
    public static final Color SAND_3 = new Color(233/255f, 252/255f, 90/255f, 1);

    public static final Color DIRT_1 = new Color(96/255f, 47/255f, 18/255f, 1);
    public static final Color DIRT_2 = new Color(135/255f, 70/255f, 32/255f, 1);
    public static final Color DIRT_3 = new Color(79/255f, 38/255f, 15/255f, 1);

    public static final Color COAL_1 = new Color(53/255f, 53/255f, 53/255f, 1);
    public static final Color COAL_2 = new Color(34/255f, 35/255f, 38/255f, 1);
    public static final Color COAL_3 = new Color(65/255f, 65/255f, 65/255f, 1);

    public static final Color EMBER = new Color(102/255f, 59/255f, 0/255f, 1);

    public static final Color GUNPOWDER_1 = new Color(255/255f, 142/255f, 142/255f, 1);
    public static final Color GUNPOWDER_2 = new Color(255/255f, 91/255f, 91/255f, 1);
    public static final Color GUNPOWDER_3 = new Color(219/255f, 160/255f, 160/255f, 1);

    public static final Color SNOW = new Color(1, 1, 1, 1);

    public static final Color PLAYERMEAT = new Color(255/255f, 255/255f, 0/255f, 1);

    // Immovable Solids
    public static final Color STONE = new Color(150/255f, 150/255f, 150/255f, 1);

    public static final Color WOOD_1 = new Color(165/255f, 98/255f, 36/255f, 1);
    public static final Color WOOD_2 = new Color(61/255f, 33/255f, 7/255f, 1);
    public static final Color WOOD_3 = new Color(140/255f, 74/255f, 12/255f, 1);

    public static final Color TITANIUM = new Color(234/255f, 234/255f, 234/255f, 1);

    public static final Color SLIME_MOLD_1 = new Color(255/255f, 142/255f, 243/255f, 1);
    public static final Color SLIME_MOLD_2 = new Color(201/255f, 58/255f, 107/255f, 1);
    public static final Color SLIME_MOLD_3 = new Color(234/255f, 35/255f, 213/255f, 1);


    public static final Color GROUND = new Color(68/255f, 37/255f, 37/255f, 1);


    // Liquids
    public static final Color WATER = new Color(28/255f, 86/255f, 234/255f, .8f);

    public static final Color OIL = new Color(55/255f, 60/255f, 73/255f, .8f);

    public static final Color ACID = new Color(0/255f, 255/255f, 0/255f, 1);

    public static final Color LAVA = new Color(255/255f, 165/255f, 0/255f, 1);

    public static final Color BLOOD = new Color(234/255f, 0 /255f,0/255f, .8f);

    public static final Color CEMENT = new Color(209/255f, 209/255f,209/255f, 1f);


    // Gasses
    public static final Color SMOKE = new Color(147/255f, 147/255f, 147/255f, 0.5f);

    public static final Color FLAMMABLE_GAS = new Color(0/255f, 255/255f, 0/255f, 0.5f);

    public static final Color SPARK = new Color(89/255f, 35/255f, 13/255f, 1);

    public static final Color STEAM_1 = new Color(204/255f, 204/255f, 204/255f, 0.8f);
    public static final Color STEAM_2 = new Color(204/255f, 204/255f, 204/255f, 0.1f);
    public static final Color STEAM_3 = new Color(204/255f, 204/255f, 204/255f, 0.45f);

    // Effects
    private static final String FIRE_NAME = "Fire";
    public static final Color FIRE_1 = new Color(89/255f, 35/255f, 13/255f, 1);
    public static final Color FIRE_2 = new Color(100/255f, 27/255f, 7/255f, 1);
    public static final Color FIRE_3 = new Color(77/255f, 10/255f, 20/255f, 1);

    // Others
    public static final Color PARTICLE = new Color(0/255f, 0/255f, 0/255f, 0);
    public static final Color BOID_1 = new Color(0/255f, 255/255f, 255/255f, 0);
    public static final Color BOID_2 = new Color(200/255f, 0/255f, 255/255f, 0);
    public static final Color BOID_3 = new Color(150/255f, 255/255f, 255/255f, 0);
    public static final Color EMPTY_CELL = new Color(0/255f, 0/255f, 0/255f, 0);

    private static final String GRASS = "Grass";
    public static final Color GRASS_1 = new Color(0, 216/155f, 93/255f, 0);
    public static final Color GRASS_2 = new Color(0, 173/155f, 75/255f, 0);
    public static final Color GRASS_3 = new Color(0, 239/155f, 103/255f, 0);

    private static final String MaterialPath = "elementtextures/";
    private static final String png = ".png";
        // Place custom textures in materialsMap
        Pixmap stonePixmap = new Pixmap(Gdx.files.internal("elementtextures/Stone.png"));
        //Assets.getPixmap("elementtextures/Stone.png");
        Pixmap woodPixmap = new Pixmap(Gdx.files.internal("elementtextures/Wood.png"));
        //Assets.getPixmap("elementtextures/Wood.png");
        
    

    public static Color getColorForElementType(ElementType elementType, int x, int y) {
        Color newcolor = new Color();        
        MaterialMap newmaterialMap;
        if (elementType.name() != null) {
            newmaterialMap = new MaterialMap(new Pixmap(Gdx.files.internal(MaterialPath + elementType.name() + png)));
            int rgb = newmaterialMap.getRGB(x, y);      
            Color color = new Color();
            Color.rgba8888ToColor(color, rgb);
            newcolor = color;
        } 
        return newcolor;
    }
}
