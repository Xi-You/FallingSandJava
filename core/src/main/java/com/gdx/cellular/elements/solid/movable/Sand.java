package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.solid.movable.MovableSolid;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ColorConstants;

public class Sand extends MovableSolid {



    public Sand(int x, int y) {
        super(x, y);
        vel = new Vector3(Math.random() > 0.5 ? -1 : 1, -124f,0f);
        frictionFactor = 0.9f;
        inertialResistance = .1f;
        mass = 150;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        return false;
    }

}
