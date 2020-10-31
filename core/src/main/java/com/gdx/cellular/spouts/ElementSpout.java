package com.gdx.cellular.spouts;

import com.gdx.cellular.elements.ElementType;

import java.util.function.Consumer;

import com.gdx.cellular.CellularMatrix.FunctionInput;

public class ElementSpout implements Spout {

    int matrixX;
    int matrixY;
    ElementType sourceElement;
    int brushSize;
    Consumer<FunctionInput> function;

    public ElementSpout(ElementType sourceElement, int matrixX, int matrixY, int brushSize, Consumer<FunctionInput> function) {
        this.matrixX = matrixX;
        this.matrixY = matrixY;
        this.sourceElement = sourceElement;
        this.brushSize = brushSize;
        this.function = function;
    }

    @Override
    public FunctionInput setFunctionInputs(FunctionInput functionInput) {
        functionInput.setInput(FunctionInput.X, matrixX);
        functionInput.setInput(FunctionInput.Y, matrixY);
        functionInput.setInput(FunctionInput.BRUSH_SIZE, brushSize);
        functionInput.setInput(FunctionInput.ELEMENT_TYPE, sourceElement);
        return functionInput;
    }

    @Override
    public Consumer<FunctionInput> getFunction() {
        return function;
    }
}
