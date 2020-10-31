package com.gdx.cellular.elements.gas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gdx.cellular.CellularAutomaton;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.solid.Solid;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.particles.Particle;

public abstract class Gas extends Element {

    public int density;
    public int dispersionRate;

    public Gas(int x, int y) {
        super(x, y);
    }

    @Override
    public void spawnSparkIfIgnited(CellularMatrix matrix) {}

    @Override
    public boolean corrode(CellularMatrix matrix) {
        return false;
    }

//    @Override
//    public void darkenColor() { }
//
//    @Override
//    public void darkenColor(float factor) { }

    @Override
    public void step(CellularMatrix matrix) {
        if (stepped.get(0) == CellularAutomaton.stepped.get(0)) return;
        stepped.flip(0);
        vel.sub(CellularAutomaton.gravity);
        vel.y = Math.min(vel.y, 124);
        if (vel.y == 124 && Math.random() > .7) {
            vel.y = 64;
        }
        vel.x *= .9;
//        if (vel.x == 0 && Math.random() > .8) {
//            vel.x = 64;
//        }

        int yModifier = vel.y < 0 ? -1 : 1;
        int xModifier = vel.x < 0 ? -1 : 1;
        float velYDeltaTimeFloat = (Math.abs(vel.y) * 1/60);
        float velXDeltaTimeFloat = (Math.abs(vel.x) * 1/60);
        int velXDeltaTime;
        int velYDeltaTime;
        if (velXDeltaTimeFloat < 1) {
            xThreshold += velXDeltaTimeFloat;
            velXDeltaTime = (int) xThreshold;
            if (Math.abs(velXDeltaTime) > 0) {
                xThreshold = 0;
            }
        } else {
            xThreshold = 0;
            velXDeltaTime = (int) velXDeltaTimeFloat;
        }
        if (velYDeltaTimeFloat < 1) {
            yThreshold += velYDeltaTimeFloat;
            velYDeltaTime = (int) yThreshold;
            if (Math.abs(velYDeltaTime) > 0) {
                yThreshold = 0;
            }
        } else {
            yThreshold = 0;
            velYDeltaTime = (int) velYDeltaTimeFloat;
        }

        boolean xDiffIsLarger = Math.abs(velXDeltaTime) > Math.abs(velYDeltaTime);

        int upperBound = Math.max(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        int min = Math.min(Math.abs(velXDeltaTime), Math.abs(velYDeltaTime));
        float floatFreq = (min == 0 || upperBound == 0) ? 0 : ((float) min / upperBound);
        int freqThreshold = 0;
        float freqCounter = 0;

        int smallerCount = 0;
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        for (int i = 1; i <= upperBound; i++) {
            freqCounter += floatFreq;
            boolean thresholdPassed = Math.floor(freqCounter) > freqThreshold;
            if (floatFreq != 0 && thresholdPassed && min >= smallerCount) {
                freqThreshold = (int) Math.floor(freqCounter);
                smallerCount += 1;
            }

            int yIncrease, xIncrease;
            if (xDiffIsLarger) {
                xIncrease = i;
                yIncrease = smallerCount;
            } else {
                yIncrease = i;
                xIncrease = smallerCount;
            }

            int modifiedMatrixY = getMatrixY() + (yIncrease * yModifier);
            int modifiedMatrixX = getMatrixX() + (xIncrease * xModifier);
            if (matrix.isWithinBounds(modifiedMatrixX, modifiedMatrixY)) {
                Element neighbor = matrix.get(modifiedMatrixX, modifiedMatrixY);
                if (neighbor == this) continue;
                boolean stopped = actOnNeighboringElement(neighbor, modifiedMatrixX, modifiedMatrixY, matrix, i == upperBound, i == 1, lastValidLocation, 0);
                if (stopped) {
                    break;
                }
                lastValidLocation.x = modifiedMatrixX;
                lastValidLocation.y = modifiedMatrixY;

            } else {
                matrix.setElementAtIndex(getMatrixX(), getMatrixY(), ElementType.EMPTYCELL.createElementByMatrix(getMatrixX(), getMatrixY()));
                return;
            }
        }
        applyHeatToNeighborsIfIgnited(matrix);
        modifyColor();
        spawnSparkIfIgnited(matrix);
        checkLifeSpan(matrix);
        takeEffectsDamage(matrix);
        if (matrix.useChunks) {
            if (isIgnited) {
                matrix.reportToChunkActive(this);
            }
        }
    }

    @Override
    protected boolean actOnNeighboringElement(Element neighbor, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, Vector3 lastValidLocation, int depth) {
        boolean acted = actOnOther(neighbor, matrix);
        if (acted) return true;
        if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
            if (isFinal) {
                swapPositions(matrix, neighbor, modifiedMatrixX, modifiedMatrixY);
            } else {
                return false;
            }
        } else if (neighbor instanceof Gas) {
            Gas gasNeighbor = (Gas) neighbor;
            if (compareGasDensities(gasNeighbor)) {
                swapGasForDensities(matrix, gasNeighbor, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                return false;
            }
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }

            vel.x = vel.x < 0 ? -62 : 62;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        } else if (neighbor instanceof Liquid) {
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (neighbor.isFreeFalling) {
                return true;
            }
            float absY = Math.max(Math.abs(vel.y) / 31, 105);
            vel.x = vel.x < 0 ? -absY : absY;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        } else if (neighbor instanceof Solid) {
            if (depth > 0) {
                return true;
            }
            if (isFinal) {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (neighbor.isFreeFalling) {
                return true;
            }

            float absY = Math.max(Math.abs(vel.y) / 31, 105);
            vel.x = vel.x < 0 ? -absY : absY;

            Vector3 normalizedVel = vel.cpy().nor();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            Element diagonalNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY() + additionalY);
            if (isFirst) {
                vel.y = getAverageVelOrGravity(vel.y, neighbor.vel.y);
            } else {
                vel.y = 124;
            }

            neighbor.vel.y = vel.y;
            vel.x *= frictionFactor;
            if (diagonalNeighbor != null) {
                boolean stoppedDiagonally = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY() + additionalY, distance);
                if (!stoppedDiagonally) {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(getMatrixX() + additionalX, getMatrixY());
            if (adjacentNeighbor != null) {
                boolean stoppedAdjacently = iterateToAdditional(matrix, getMatrixX() + additionalX, getMatrixY(), distance);
                if (stoppedAdjacently) vel.x *= -1;
                if (!stoppedAdjacently) {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        return false;
    }

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance) {
        int distanceModifier = distance > 0 ? 1 : -1;
        Vector3 lastValidLocation = new Vector3(getMatrixX(), getMatrixY(), 0);
        for (int i = 0; i <= Math.abs(distance); i++) {
            Element neighbor = matrix.get(startingX + i * distanceModifier, startingY);
            boolean acted = actOnOther(neighbor, matrix);
            if (acted) return false;
            boolean isFirst = i == 0;
            boolean isFinal = i == Math.abs(distance);
            if (neighbor == null) continue;
            if (neighbor instanceof EmptyCell || neighbor instanceof Particle) {
                if (isFinal) {
                    swapPositions(matrix, neighbor, startingX + i * distanceModifier, startingY);
                    return false;
                }
                lastValidLocation.x = startingX + i * distanceModifier;
                lastValidLocation.y = startingY;
                continue;
            } else if (neighbor instanceof Gas) {
                Gas gasNeighbor = (Gas) neighbor;
                if (compareGasDensities(gasNeighbor)) {
                    swapGasForDensities(matrix, gasNeighbor, startingX + i * distanceModifier, startingY,  lastValidLocation);
                    return false;
                }
            } else if (neighbor instanceof Solid || neighbor instanceof Liquid) {
                if (isFirst) {
                    return true;
                }
                moveToLastValid(matrix, lastValidLocation);
                return false;
            }
        }
        return true;
    }

    private void swapGasForDensities(CellularMatrix matrix, Gas neighbor, int neighborX, int neighborY, Vector3 lastValidLocation) {
        vel.y = 62;
        moveToLastValidAndSwap(matrix, neighbor, neighborX, neighborY, lastValidLocation);
    }

    private boolean compareGasDensities(Gas neighbor) {
        return (density > neighbor.density && neighbor.getMatrixY() <= getMatrixY()); // ||  (density < neighbor.density && neighbor.matrixY >= matrixY);
    }

    private int getAdditional(float val) {
        if (val < -.1f) {
            return (int) Math.floor(val);
        } else if (val > .1f) {
            return (int) Math.ceil(val);
        } else {
            return 0;
        }
    }

    private float getAverageVelOrGravity(float vel, float otherVel) {
        if (otherVel > 125f) {
            return 124f;
        }
        float avg = (vel + otherVel) / 2;
        if (avg > 0) {
            return avg;
        } else {
            return Math.min(avg, 124f);
        }
    }

    @Override
    public boolean infect(CellularMatrix matrix) {
        return false;
    }
    
    @Override
    public boolean stain(float r, float g, float b, float a) {
        return false;
    }

    @Override
    public boolean stain(Color color) {
        return  false;
    }
}
