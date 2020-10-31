package com.gdx.cellular.elements.liquid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ColorConstants;

public class Blood extends Liquid{

    public Blood(int x, int y) {
        super(x, y);
        vel = new Vector3(0,-124f,0);
        inertialResistance = 0;
        mass = 100;
        frictionFactor = 1f;
        density = 6;
        dispersionRate = 5;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        coolingFactor = 5;
    }
    @Override
    public boolean receiveHeat(int heat) {
        return false;
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix) {
        other.stain(new Color(0.5f, 0, 0, 1));
        if (other.shouldApplyHeat()) {
            other.receiveCooling(matrix, coolingFactor);
            coolingFactor--;
            if (coolingFactor <= 0) {
                die(matrix);
                return true;
            }
            return false;
        }
        return false;
    }
}
