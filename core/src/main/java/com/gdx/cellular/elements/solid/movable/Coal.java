package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ColorConstants;


public class Coal extends MovableSolid {



    public Coal(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = .4f;
        inertialResistance = .8f;
        mass = 200;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        flammabilityResistance = 100;
        resetFlammabilityResistance = 35;
    }

    @Override
    public void spawnSparkIfIgnited(CellularMatrix matrix) {
        if (getRandomInt(20) > 2) return;
        super.spawnSparkIfIgnited(matrix);
    }
}
