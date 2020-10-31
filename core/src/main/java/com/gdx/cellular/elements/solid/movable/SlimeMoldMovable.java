package com.gdx.cellular.elements.solid.movable;

import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.effects.EffectColors;
import com.badlogic.gdx.graphics.Color;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.ColorConstants;

public class SlimeMoldMovable extends MovableSolid {

    public SlimeMoldMovable(int x, int y) {
        super(x, y);
        vel = new Vector3(0f, -124f,0f);
        frictionFactor = 0.5f;
        inertialResistance = 1.1f;
        mass = 500;        
        color = ColorConstants.getColorForElementType(this.elementType, x, y);
        flammabilityResistance = 10;
        resetFlammabilityResistance = 0;
        health = 40;
    }

    @Override
    public void step(CellularMatrix matrix) {
        super.step(matrix);
        infectNeighbors(matrix);
    }

    private boolean infectNeighbors(CellularMatrix matrix) {
        if (!isEffectsFrame() || isIgnited) return false;
        boolean shouldDie = false;
        for (int x = matrixX - 1; x <= matrixX + 1; x++) {
            for (int y = matrixY - 1; y <= matrixY + 1; y++) {
                if (!(x == 0 && y == 0)) {
                    Element neighbor = matrix.get(x, y);
                    if (neighbor != null) {
                        shouldDie = neighbor.infect(matrix) || shouldDie;
                    }
                }
            }
        }
        if (shouldDie) {
            this.dieAndReplace(matrix, ElementType.SLIMEMOLD);
        }
        return true;
    }

    @Override
    public void takeFireDamage(CellularMatrix matrix) {
        health -= fireDamage;
        checkIfIgnited();
    }

    public boolean infect(CellularMatrix matrix) {
        return false;
    }

}
