package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.CellularMatrix;

public class FlammableGas extends Gas {

    public FlammableGas(int x, int y) {
        super(x, y);
        health = 100;
        vel = new Vector3(0,124f,0);
        inertialResistance = 0;
        mass = 1;
        frictionFactor = 1f;
        density = 1;
        dispersionRate = 2;
        color = Color.GREEN;
        defaultColor = Color.GREEN;
        lifeSpan = getRandomInt(500) + 1000;
        flammabilityResistance = 10;
        resetFlammabilityResistance = 10;
    }
    
}
