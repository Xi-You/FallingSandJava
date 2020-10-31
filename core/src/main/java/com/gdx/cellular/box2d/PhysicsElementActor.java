package com.gdx.cellular.box2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.movable.MovableSolid;

public class PhysicsElementActor {

    Array<Array<Element>> elements;
    Body physicsBody;
    Vector2 lastPos = new Vector2(0,0);
    float xAccumulator = 0;
    float yAccumulator = 0;
    float angleAccumulator = 0;
    float lastAngle = 0;
    int xCenterOffset;
    int yCenterOffset;
    int shouldCalculateCount = 3;
    int recalculateCount = 0;
    final int recalculateThreshold = 3;
    private boolean shouldRecalculateBoundaries = false;

    public PhysicsElementActor(Body body, Array<Array<Element>> elements, int minX, int maxY) {
        this.physicsBody = body;
        this.elements = elements;
        xCenterOffset = (int) Math.abs(body.getPosition().x*5 - minX) + 1;
        yCenterOffset = (int) Math.abs(body.getPosition().y*5 - maxY) - 1;
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element != null) {
                    element.owningBody = this;
                    element.setOwningBodyCoords(x - xCenterOffset, y - yCenterOffset);
                }
            }
        }
    }

    public void step(CellularMatrix matrix) {
        if (Math.abs(physicsBody.getWorldCenter().y) > 200 || Math.abs(physicsBody.getWorldCenter().x) > 200) {
            matrix.destroyPhysicsElementActor(this);
            return;
        }
        if (shouldRecalculateBoundaries) {
            recalculateCount++;
            if (recalculateCount > recalculateThreshold) {
                recalculateBoundaries();
                recalculateCount = 0;
                shouldRecalculateBoundaries = false;
            }
        }
        xAccumulator += Math.abs(physicsBody.getPosition().x - lastPos.x);
        yAccumulator += Math.abs(physicsBody.getPosition().y - lastPos.y);
        angleAccumulator += Math.abs(physicsBody.getAngle() - lastAngle);
        if (shouldCalculateCount > 0 || xAccumulator > .2 || yAccumulator > .2 | angleAccumulator > .7) {
            for (int y = 0; y < elements.size; y++) {
                Array<Element> row = elements.get(y);
                for (int x = 0; x < row.size; x++) {
                    Element element = row.get(x);
                    if (element != null) {
                        if (element.secondaryMatrixCoords.size() > 0) {
                            for (int i = 0; i < element.secondaryMatrixCoords.size(); i++) {

                                matrix.setElementAtIndex((int) new Vector2(element.secondaryMatrixCoords.get(i)).x, (int) new Vector2(element.secondaryMatrixCoords.get(i)).y, ElementType.EMPTYCELL.createElementByMatrix(0,0));

                            }
                          //  element.secondaryMatrixCoords.forEach(vector2 -> matrix.setElementAtIndex((int) vector2.x, (int) vector2.y, ElementType.EMPTYCELL.createElementByMatrix(0,0)));
                            element.resetSecondaryCoordinates();
                        }
                        Vector2 matrixCoords = getMatrixCoords(element);
                        matrix.reportToChunkActive((int) matrixCoords.x, (int) matrixCoords.y);
                        Element elementAtNewPos = matrix.get((int) matrixCoords.x, (int) matrixCoords.y);
                        if (elementAtNewPos == element) {
                            continue;
                        }
                        if (elementAtNewPos == null) {
                            matrix.setElementAtIndex(element.getMatrixX(), element.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(element.getMatrixX(), element.getMatrixY()));
                            continue;
                        }
                        if (elementAtNewPos.owningBody != null) {
                            elementAtNewPos.owningBody.shouldCalculateCount = 2;
                        }
                        if (elementAtNewPos instanceof EmptyCell || elementAtNewPos.owningBody == this) {
                            matrix.setElementAtIndex(element.getMatrixX(), element.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(element.getMatrixX(), element.getMatrixY()));
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
                        } else if (elementAtNewPos instanceof MovableSolid || elementAtNewPos instanceof Liquid) {
                            elementAtNewPos.dieAndReplaceWithParticle(matrix, matrix.generateRandomVelocityWithBounds(-100, 100));
                            physicsBody.setLinearVelocity(physicsBody.getLinearVelocity().scl(.9f));
                            physicsBody.setAngularVelocity(physicsBody.getAngularVelocity() * .98f);
                            if (matrix.isWithinBounds(matrixCoords)) {
                                matrix.setElementAtIndex((int) matrixCoords.x, (int) matrixCoords.y, element);
                            }
                        } else {
                            matrix.setElementAtIndex(element.getMatrixX(), element.getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(element.getMatrixX(), element.getMatrixY()));
                        }
                    }
                }
            }
            this.lastAngle = physicsBody.getAngle();
            this.lastPos = physicsBody.getPosition().cpy();
            xAccumulator = 0;
            yAccumulator = 0;
            angleAccumulator = 0;
            shouldCalculateCount -= 1;
//            int drawLength = 2;
//            for (int y = 0; y < elements.size; y++) {
//                Array<Element> row = elements.get(y);
//                for (int x = 0; x < row.size - drawLength; x++) {
//                    Element element = row.get(x);
//                    if (element != null) {
//                        for (int length = 1; length <= drawLength; length++) {
//                            Element nextElement = row.get(x + length);
//                            if (nextElement == null) continue;
//                            if ((element.matrixX - nextElement.matrixX != length)) {
//                                matrix.setElementAtSecondLocation(element.matrixX + length, element.matrixY, element);
//                            }
//                        }
//                    }
//                }
//            }
        } else {
            this.lastAngle = physicsBody.getAngle();
            this.lastPos = physicsBody.getPosition().cpy();
        }
    }

    public void draw(ShapeRenderer sr) {
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < elements.size; y++) {
            Array<Element> row = elements.get(y);
            for (int x = 0; x < row.size; x ++) {
                Element element = row.get(x);
                if (element != null) {
                    sr.setColor(element.color);
                    sr.rect(element.toPixel(element.getMatrixX()), element.toPixel(element.getMatrixY()), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);
                    for (int i = 0; i < element.secondaryMatrixCoords.size(); i++) {

                        sr.rect(element.toPixel((int) new Vector2(element.secondaryMatrixCoords.get(i)).x), element.toPixel((int) new Vector2(element.secondaryMatrixCoords.get(i)).y), CellularAutomaton.pixelSizeModifier, CellularAutomaton.pixelSizeModifier);

                    }
                    
                }
            }
        }
        sr.end();
    }

    public Vector2 getMatrixCoords(Element element) {
        Vector2 bodyPos = physicsBody.getPosition();
        int bodyCenterMatrixX = (int) ((bodyPos.x * CellularAutomaton.box2dSizeModifier)/2);
        int bodyCenterMatrixY = (int) ((bodyPos.y * CellularAutomaton.box2dSizeModifier)/2);
        Vector2 matrixPoint = new Vector2(bodyCenterMatrixX + element.owningBodyCoords.x, bodyCenterMatrixY - element.owningBodyCoords.y);
        float angle = physicsBody.getAngle();
        float newX = (float) (((matrixPoint.x-bodyCenterMatrixX) * Math.cos(angle) - (matrixPoint.y-bodyCenterMatrixY) * Math.sin(angle)) + bodyCenterMatrixX);
        float newY = (float) (((matrixPoint.y-bodyCenterMatrixY) * Math.cos(angle) + (matrixPoint.x-bodyCenterMatrixX) * Math.sin(angle)) + bodyCenterMatrixY);
        matrixPoint.x = Math.round(newX);
        matrixPoint.y = Math.round(newY);
        return matrixPoint;
    }

    public boolean elementDeath(Element elementToDie, Element replacement) {
        Element newReplacement = replacement;
        if (replacement instanceof EmptyCell) {
            newReplacement = null;
        }
        this.elements.get((int) elementToDie.owningBodyCoords.y + yCenterOffset).set((int) elementToDie.owningBodyCoords.x + xCenterOffset, newReplacement);
        if (newReplacement != null) {
            newReplacement.owningBody = this;
            newReplacement.setOwningBodyCoords(elementToDie.owningBodyCoords);
        }
        shouldRecalculateBoundaries = true;
        shouldCalculateCount = 2;
        return true;
    }

    public void recalculateBoundaries() {
        Body newBody = ShapeFactory.createPolygonFromElementArrayDeleteOldBody((int) this.physicsBody.getPosition().x, (int) this.physicsBody.getPosition().y, this.elements, this.physicsBody);
        if (newBody == null) return;
        newBody.setAngularVelocity(this.physicsBody.getAngularVelocity());
        newBody.setLinearVelocity(this.physicsBody.getLinearVelocity());
        newBody.setTransform(this.physicsBody.getTransform().getPosition(), this.physicsBody.getTransform().getRotation());
        this.physicsBody = newBody;
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }
}
