package com.gdx.cellular.elements.solid.immoveable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ColorConstants;
import com.gdx.cellular.elements.ElementType;

public class Stone extends ImmovableSolid {




    public Stone(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, 0f,0f);
        frictionFactor = 0.5f;
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        inertialResistance = 1.1f;
        mass = 500;
        explosionResistance = 4;
    }
    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        this.temperature += heat;
        checkIfDead(matrix);
        return true;
    }

    @Override
    public void checkIfDead(CellularMatrix matrix) {
        if (this.temperature >= 800) {
            dieAndReplace(matrix, ElementType.LAVA);            
        }        
    }

}
