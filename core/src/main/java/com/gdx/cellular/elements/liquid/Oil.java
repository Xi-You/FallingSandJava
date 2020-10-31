package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ColorConstants;

public class Oil extends Liquid {
    public Oil(int x, int y) {
        super(x, y);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 75;
        frictionFactor = 1f;
        density = 4;
        dispersionRate = 4;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        flammabilityResistance = 5;
        resetFlammabilityResistance = 2;
        fireDamage = 10;
        temperature = 10;
        health = 1000;
    }
}
