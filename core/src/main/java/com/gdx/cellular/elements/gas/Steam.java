package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.Element;
import com.badlogic.gdx.graphics.Color;
import com.gdx.cellular.elements.ColorConstants;

public class Steam extends Gas {



    public Steam(int x, int y) {
        super(x, y);
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        frictionFactor = 1f;
        density = 5;
        dispersionRate = 2;
        lifeSpan = getRandomInt(2000) + 1000;
    }



    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }
}
