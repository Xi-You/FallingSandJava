package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.ColorConstants;

public class Smoke extends Gas{

    public Smoke(int x, int y) {
        super(x, y);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        frictionFactor = 1f;
        density = 3;
        dispersionRate = 2;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        lifeSpan = getRandomInt(250) + 450;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }
}
