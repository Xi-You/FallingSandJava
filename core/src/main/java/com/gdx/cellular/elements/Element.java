package com.gdx.cellular.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.EffectColors;
import com.gdx.cellular.box2d.PhysicsElementActor;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class Element {

    private static final int REACTION_FRAME = 3;
    public static final int EFFECTS_FRAME = 1;
    public int pixelX;
    public int pixelY;

    public int matrixX;
    public int matrixY;
    public Vector3 vel;

    public List<Vector2> secondaryMatrixCoords = new ArrayList<>();

    public float frictionFactor;
    public boolean isFreeFalling = true;
    public float inertialResistance;
    public int mass;
    public int health = 500;
    public int flammabilityResistance = 100;
    public int resetFlammabilityResistance = flammabilityResistance / 2;
    public boolean isIgnited;
    public int heatFactor = 10;
    public int fireDamage = 3;
    public boolean heated = false;
    public int temperature = 0;
    public int coolingFactor = 5;
    public Integer lifeSpan = null;
    public int stoppedMovingCount = 0;
    public int stoppedMovingThreshold = 1;
    public ElementType elementType;
    public PhysicsElementActor owningBody = null;
    public Vector2 owningBodyCoords = null;
    public int explosionResistance = 1;
    public int explosionRadius = 0;
    public boolean discolored = false;
    
    public Color defaultColor = Color.WHITE;

    public Color color;

    public float xThreshold = 0;
    public float yThreshold = 0;
    
    public boolean isDead = false;

    public BitSet stepped = new BitSet(1);

    public Element(int x, int y) {
        setCoordinatesByMatrix(x, y);
        this.elementType = getEnumType();
        // this.color = ColorConstants.getColorForElementType(this.elementType, x, y);
        //this.color = ColorConstants.getColorForElementType(this.elementType, x, y);
        stepped.set(0, CellularAutomaton.stepped.get(0));
    }

//    public abstract void draw(ShapeRenderer sr);

    public void setVelocity(Vector3 vel) {
        this.vel = vel;
    }

    public abstract void step(CellularMatrix matrix);

    public boolean actOnOther(Element other, CellularMatrix matrix) {
        return false;
    }

    protected abstract boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth);

    public void swapPositions(CellularMatrix matrix, Element toSwap) {
    swapPositions(matrix, toSwap, toSwap.getMatrixX(), toSwap.getMatrixY());
    }

    public void swapPositions(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY) {
if (this.getMatrixX() == toSwapX && this.getMatrixY() == toSwapY) {
            return;
        }
        matrix.setElementAtIndex(this.getMatrixX(), this.getMatrixY(), toSwap);
        matrix.setElementAtIndex(toSwapX, toSwapY, this);
    }

    public void moveToLastValid(CellularMatrix matrix, Vector3 moveToLocation) {
         if ((int) (moveToLocation.x) == getMatrixX() && (int) (moveToLocation.y) == getMatrixY()) return;
        Element toSwap = matrix.get(moveToLocation.x, moveToLocation.y);
        swapPositions(matrix, toSwap, (int) moveToLocation.x, (int) moveToLocation.y);
    }

    public void moveToLastValidAndSwap(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY, Vector3 moveToLocation) {
        int moveToLocationMatrixX = (int) moveToLocation.x;
        int moveToLocationMatrixY = (int) moveToLocation.y;
        Element thirdNeighbor = matrix.get(moveToLocationMatrixX, moveToLocationMatrixY);
if (this == thirdNeighbor || thirdNeighbor == toSwap) {
            this.swapPositions(matrix, toSwap, toSwapX, toSwapY);
            return;
        }

        if (this == toSwap) {
            this.swapPositions(matrix, thirdNeighbor, moveToLocationMatrixX, moveToLocationMatrixY);
            return;
        }
        matrix.setElementAtIndex(this.getMatrixX(), this.getMatrixY(), thirdNeighbor);
        matrix.setElementAtIndex(toSwapX, toSwapY, this);
        matrix.setElementAtIndex(moveToLocationMatrixX, moveToLocationMatrixY, toSwap);
    }

    public void setSecondaryCoordinatesByMatrix(int providedX, int providedY) {
        this.secondaryMatrixCoords.add(new Vector2(providedX, providedY));
    }

    public void setCoordinatesByMatrix(Vector2 pos) {
        setCoordinatesByMatrix((int) pos.x, (int) pos.y);
    }

    public void setCoordinatesByMatrix(int providedX, int providedY) {
        setXByMatrix(providedX);
        setYByMatrix(providedY);
    }

    public void setCoordinatesByPixel(int providedX, int providedY) {
        setXByPixel(providedX);
        setYByPixel(providedY);
    }

    public void setXByPixel(int providedVal) {
        this.pixelX = providedVal;
        this.matrixX = toMatrix(providedVal);
    }

    public void setYByPixel(int providedVal) {
        this.pixelY = providedVal;
        this.matrixY = toMatrix(providedVal);
    }

    public void setXByMatrix(int providedVal) {
        this.setMatrixX(providedVal);
        this.pixelX = toPixel(providedVal);
    }

    public void setYByMatrix(int providedVal) {
        this.setMatrixY(providedVal);
        this.pixelY = toPixel(providedVal);
    }

    public int toMatrix(int pixelVal) {
        return (int) Math.floor(pixelVal / CellularAutomaton.pixelSizeModifier);
    }

    public int toPixel(int matrixVal) {
        return (int) Math.floor(matrixVal * CellularAutomaton.pixelSizeModifier);
    }

    public boolean isReactionFrame() {
        return CellularAutomaton.frameCount == REACTION_FRAME;
    }

    public boolean isEffectsFrame() {
        return CellularAutomaton.frameCount == EFFECTS_FRAME;
    }

    public boolean corrode(CellularMatrix matrix) {
        this.health -= 170;
        checkIfDead(matrix);
        return true;
    }

    public boolean applyHeatToNeighborsIfIgnited(CellularMatrix matrix) {
        if (!isEffectsFrame() || !shouldApplyHeat()) return false;
        for (int x = getMatrixX() - 1; x <= getMatrixX() + 1; x++) {
            for (int y = getMatrixY() - 1; y <= getMatrixY() + 1; y++) {
                if (!(x == 0 && y == 0)) {
                    Element neighbor = matrix.get(x, y);
                    if (neighbor != null) {
                        neighbor.receiveHeat(matrix, heatFactor);
                    }
                }
            }
        }
        return true;
    }

    public boolean shouldApplyHeat() {
        return isIgnited || heated;
    }

    public boolean receiveHeat(int heat) {
        if (isIgnited) {
            return false;
        }
        this.flammabilityResistance -= (int) (Math.random() * heat);
        checkIfIgnited();
        return true;
    }

    public boolean receiveCooling(CellularMatrix matrix, int cooling) {
        if (isIgnited) {
            this.flammabilityResistance += cooling;
            checkIfIgnited();
            return true;
        }
        return false;
    }

    public void checkIfIgnited() {
        if (this.flammabilityResistance <= 0) {
            this.isIgnited = true;
            modifyColor();
        } else {
            this.isIgnited = false;
            // this.color = ColorConstants.getColorForElementType(elementType, this.getMatrixX(), this.getMatrixY());
            this.color = defaultColor;
        }
    }

    public void checkIfDead(CellularMatrix matrix) {
        if (this.health <= 0) {
            die(matrix);
        }
    }

    public void die(CellularMatrix matrix) {
        die(matrix, ElementType.EMPTYCELL, true);
    }

    public void dieWithBody(CellularMatrix matrix) {
        die(matrix, ElementType.EMPTYCELL, false);
    }

    public void die(CellularMatrix matrix, ElementType type, boolean Ignorebody) {
    this.isDead = true;            
        Element newElement = type.createElementByMatrix(getMatrixX(), getMatrixY());
        Element emptycell = ElementType.EMPTYCELL.createElementByMatrix(matrixX, matrixY);
        matrix.setElementAtIndex(getMatrixX(), getMatrixY(), newElement);
        matrix.reportToChunkActive(getMatrixX(), getMatrixY());

        if (owningBody != null) {
            if (!Ignorebody) {
                owningBody.elementDeath(this, newElement);
                //  if (secondaryMatrixCoords.size() > 0) {

                for (int i = 0; i < secondaryMatrixCoords.size(); i++) {

                    matrix.setElementAtIndex((int) new Vector2(secondaryMatrixCoords.get(i)).x, (int) new Vector2(secondaryMatrixCoords.get(i)).y, newElement);

                }
            } else {
                owningBody.elementDeath(this, emptycell);
                //  if (secondaryMatrixCoords.size() > 0) {

                for (int i = 0; i < secondaryMatrixCoords.size(); i++) {

                    matrix.setElementAtIndex((int) new Vector2(secondaryMatrixCoords.get(i)).x, (int) new Vector2(secondaryMatrixCoords.get(i)).y, emptycell);

                }
                //   secondaryMatrixCoords.stream().forEach(vector2 -> matrix.setElementAtIndex((int) vector2.x, (int) vector2.y, newElement));
                //    }
            }        
        }
    }

    public void dieAndReplaceWithBody(CellularMatrix matrix, ElementType type) {
        die(matrix, type, false);
    }

    public void dieAndReplace(CellularMatrix matrix, ElementType type) {
        die(matrix, type, true);
    }

    public void dieAndReplaceWithParticle(CellularMatrix matrix, Vector3 velocity) {
        matrix.setElementAtIndex(getMatrixX(), getMatrixY(), ElementType.createParticleByMatrix(matrix, getMatrixX(), getMatrixY(), velocity, elementType, this.color, this.isIgnited));
        matrix.reportToChunkActive(getMatrixX(), getMatrixY());
    }
    
    public boolean didNotMove(Vector3 formerLocation) {       
        return formerLocation.x == getMatrixX() && formerLocation.y == getMatrixY();
    }

    public void takeEffectsDamage(CellularMatrix matrix) {
        if (!isEffectsFrame()) {
            return;
        }
        if (isIgnited) {
            takeFireDamage(matrix);
        }
        checkIfDead(matrix);
    }

    public void takeFireDamage(CellularMatrix matrix) {
        health -= fireDamage;
        if (isSurrounded(matrix)) {
            flammabilityResistance = resetFlammabilityResistance;
        }
        checkIfIgnited();
    }

    public void spawnSparkIfIgnited(CellularMatrix matrix) {
        if (!isEffectsFrame() || !isIgnited) return;
        Element upNeighbor = matrix.get(getMatrixX(), +getMatrixY() + 1);
        if (upNeighbor != null) {
            if (upNeighbor instanceof EmptyCell) {
                ElementType elementToSpawn = Math.random() > .1 ? ElementType.SPARK : ElementType.SMOKE;
//                ElementType elementToSpawn = ElementType.SPARK;
                matrix.spawnElementByMatrix(getMatrixX(), getMatrixY() + 1, elementToSpawn);
            }
        }
    }

    public void modifyColor() {
        if (isIgnited) {
            color = EffectColors.getRandomFireColor();
        }
    }

    public void checkLifeSpan(CellularMatrix matrix) {
        if (lifeSpan != null) {
            lifeSpan--;
            if (lifeSpan <= 0) {
                die(matrix);
            }
        }
    }

    public int getRandomInt(int limit) {
        return (int) (Math.random() * limit);
    }

    public boolean receiveHeat(CellularMatrix matrix, int heat) {
        if (isIgnited) {
            return false;
        }
        this.flammabilityResistance -= (int) (Math.random() * heat);
        checkIfIgnited();
        return true;
    }

    public ElementType getEnumType() {
        return ElementType.valueOf(this.getClass().getSimpleName().toUpperCase());
    }

    public boolean infect(CellularMatrix matrix) {
        if (Math.random() > 0.95f) {
            this.dieAndReplace(matrix, ElementType.SLIMEMOLD);
            return true;
        }
        return false;
    }

    public int getMatrixY() {
        return matrixY;
    }

    public void setMatrixY(int matrixY) {
        this.matrixY = matrixY;
    }

    public int getMatrixX() {
        return matrixX;
    }

    public void setMatrixX(int matrixX) {
        this.matrixX = matrixX;
    }

    public boolean hasNotMovedBeyondThreshold() {
        return stoppedMovingCount >= stoppedMovingThreshold;
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation) {
        moveToLastValidDieAndReplace(matrix, moveToLocation, this.elementType);
    }

    public void moveToLastValidDieAndReplace(CellularMatrix matrix, Vector3 moveToLocation, ElementType elementType) {
        matrix.setElementAtIndex((int) moveToLocation.x, (int) moveToLocation.y, elementType.createElementByMatrix((int) moveToLocation.x, (int) moveToLocation.y));
        die(matrix);
    }





    public void resetSecondaryCoordinates() {
        this.secondaryMatrixCoords = new ArrayList<>();
    }

    public void magmatize(CellularMatrix matrix, int damage) {
        this.health -= damage;
        checkIfDead(matrix);
    }

    public void setOwningBodyCoords(int x, int y) {
        this.owningBodyCoords = new Vector2(x, y);
    }

    public void setOwningBodyCoords(Vector2 coords) {
        setOwningBodyCoords((int) coords.x, (int) coords.y);
    }

//    public void setCoordinatesByMatrix(Vector2 pos) {
//        setCoordinatesByMatrix((int) pos.x, (int) pos.y);
//    }

    public void customElementFunctions(CellularMatrix matrix) { }

    public boolean explode(CellularMatrix matrix, int strength) {
        if (explosionResistance < strength) {
            if (Math.random() > 0.3) {
                dieAndReplace(matrix, ElementType.EXPLOSIONSPARK);
            } else {
                die(matrix);
            }
            return true;
        } else {
            darkenColor();
            return false;
        }
    }

    public void darkenColor() {
        this.color = new Color(this.color.r * .85f, this.color.g * .85f, this.color.b * .85f, this.color.a);
        this.discolored = true;
    }

    public void darkenColor(float factor) {
        this.color = new Color(this.color.r * factor, this.color.g * factor, this.color.b * factor, this.color.a);
        this.discolored = true;
    }

    public boolean isDead() {
        return isDead;
    }
    
        public boolean stain(Color color) {
        if (Math.random() > 0.2 || isIgnited) {
            return false;
        }
        this.color = color.cpy();
        this.discolored = true;
        return true;
    }

    public boolean stain(float r, float g, float b, float a) {
        if (Math.random() > 0.2 || isIgnited) {
            return false;
        }
        this.color = this.color.cpy();
        this.color.r += r;
        this.color.g += g;
        this.color.b += b;
        this.color.a += a;
        if (this.color.r > 1f) {
            this.color.r = 1f;
        }
        if (this.color.g > 1f) {
            this.color.g = 1f;
        }
        if (this.color.b > 1f) {
            this.color.b = 1f;
        }
        if (this.color.a > 1f) {
            this.color.a = 1f;
        }
        if (this.color.r < 0f) {
            this.color.r = 0f;
        }
        if (this.color.g < 0f) {
            this.color.g = 0f;
        }
        if (this.color.b < 0f) {
            this.color.b = 0f;
        }
        if (this.color.a < 0f) {
            this.color.a = 0f;
        }
        this.discolored = true;
        return true;
    }

    public boolean cleanColor() {
        if (!discolored || Math.random() > 0.2f) {
            return false;
        }
        this.color = ColorConstants.getColorForElementType(this.elementType, this.getMatrixX(), this.getMatrixY());
        this.discolored = false;
        return true;
    }

private boolean isSurrounded(CellularMatrix matrix) {
      //  if (matrix.get(this.matrixX, this.matrixY + 1) instanceof EmptyCell) {
        if (matrix.get(this.getMatrixX(), this.getMatrixY() + 1) instanceof EmptyCell) {
            return false;
      //  } else if (matrix.get(this.matrixX + 1, this.matrixY) instanceof EmptyCell) {
        } else if (matrix.get(this.getMatrixX() + 1, this.getMatrixY()) instanceof EmptyCell) {
            return false;
     //   } else if (matrix.get(this.matrixX - 1, this.matrixY) instanceof EmptyCell) {
        } else if (matrix.get(this.getMatrixX() - 1, this.getMatrixY()) instanceof EmptyCell) {
            return false;
     //   } else if (matrix.get(this.matrixX, this.matrixY - 1) instanceof EmptyCell) {
        } else if (matrix.get(this.getMatrixX(), this.getMatrixY() - 1) instanceof EmptyCell) {
            return false;
        }
        return true;
    }
}
