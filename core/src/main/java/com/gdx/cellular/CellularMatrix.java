package com.gdx.cellular;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.util.Chunk;
import com.gdx.cellular.elements.gas.Gas;
import com.gdx.cellular.elements.liquid.Liquid;
import com.gdx.cellular.elements.solid.immoveable.ImmovableSolid;
import com.gdx.cellular.elements.solid.movable.MovableSolid;
import com.gdx.cellular.particles.Particle;
import com.gdx.cellular.boids.Boid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.gdx.cellular.elements.EmptyCell;
import java.util.concurrent.ThreadLocalRandom;
import com.gdx.cellular.box2d.PhysicsElementActor;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.gdx.cellular.particles.Explosion;
import com.gdx.cellular.elements.player.PlayerMeat;
import java.util.HashSet;
import java.util.Set;

public class CellularMatrix {

    public int innerArraySize;
    public int outerArraySize;
    public int pixelSizeModifier;
    private List<Integer> shuffledXIndexes;
    private List<List<Integer>> shuffledXIndexesForThreads;
    public boolean useChunks = true;
    public boolean debugChunks = false;
    private int threadedIndexOffset = 0;

    private Array<Array<Element>> matrix;
    private Array<Array<Chunk>> chunks;
    private Array<Spout> spoutArray;
    public Array<PhysicsElementActor> physicsElementActors = new Array<>();
    public Array<Explosion> explosionArray = new Array<>();
    public Array<Boid> boids = new Array<>();
    
    public boolean useBoidChunks = false;


    public CellularMatrix(int width, int height, int pixelSizeModifier) {
        this.pixelSizeModifier = pixelSizeModifier;
        
        this.innerArraySize = toMatrix(width);
        this.outerArraySize = toMatrix(height);
        this.matrix = generateMatrix();
        
        
        
       
            this.chunks = generateChunks();
        
        this.shuffledXIndexes = generateShuffledIndexes(innerArraySize);

        calculateAndSetThreadedXIndexOffset();
        spoutArray = new Array<>();
    }

     private Array<Array<Chunk>> generateChunks() {
        Array<Array<Chunk>> chunks = new Array<>();
        int rows = (int) Math.ceil((double) outerArraySize / Chunk.size);
        int columns = (int) Math.ceil((double) innerArraySize / Chunk.size);
        for (int r = 0; r < rows; r++) {
            chunks.add(new Array<>());
            for (int c = 0; c < columns; c++) {
                int xPos = c * Chunk.size;
                int yPos = r * Chunk.size;
                Chunk newChunk = new Chunk();
                chunks.get(r).add(newChunk);
                newChunk.setTopLeft(new Vector3(Math.min(xPos, innerArraySize), Math.min(yPos, outerArraySize), 0));
                newChunk.setBottomRight(new Vector3(Math.min(xPos + Chunk.size, innerArraySize), Math.min(yPos + Chunk.size, outerArraySize), 0));
            }
        }
        return chunks;
    }

    public void calculateAndSetThreadedXIndexOffset() {
        if (shuffledXIndexesForThreads != null) {
            threadedIndexOffset = (int) (Math.random() * (innerArraySize / shuffledXIndexesForThreads.size()));
        } else {
            threadedIndexOffset = 0;
        }
    }

    private Array<Array<Element>> generateMatrix() {
        Array<Array<Element>> outerArray = new Array<>(true, outerArraySize);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> innerArr = new Array<>(true, innerArraySize);
            for (int x = 0; x < innerArraySize; x++) {
                innerArr.add(ElementType.EMPTYCELL.createElementByMatrix(x, y));
            }
            outerArray.add(innerArr);
        }
        return outerArray;
    }

    public void stepAndDrawAll(ShapeRenderer sr) {
        stepAll();
        drawAll(sr);
    }

    private void stepAll() {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void drawAll(ShapeRenderer sr) {
        drawElements(sr);
        if (debugChunks){
        drawChunks(sr);
        }
    }

    private void drawElements(ShapeRenderer sr) {
       sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                if (element.owningBody != null) {
                    continue;
                }
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    Element followingElement = row.get(following);
                    if (!followingElement.color.equals(currentColor)) {
                        break;
                    }
                    toIndex = following;
                }

                sr.setColor(element.color);
                sr.rect(element.toPixel(x), element.toPixel(y), rectDrawWidth(toIndex), pixelSizeModifier);
                x = toIndex;

            }
        }
        sr.end();
         }

    private void drawChunks(ShapeRenderer sr) {
        sr.setColor(Color.WHITE);
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int y = 0; y < chunks.size; y++) {
            Array<Chunk> chunkRow = chunks.get(y);
            for (int x = 0; x < chunkRow.size; x++) {
                Chunk chunk = chunkRow.get(x);
                if (chunk.getShouldStep()) {
                    sr.rect(chunk.getTopLeft().x * pixelSizeModifier, chunk.getTopLeft().y * pixelSizeModifier, Chunk.size * pixelSizeModifier, Chunk.size * pixelSizeModifier);
                }
            }
        }
        sr.end();
    }

public void drawBox2d(ShapeRenderer sr, Array<Body> bodies) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.RED);
        int mod = CellularAutomaton.box2dSizeModifier;
        for (Body body : bodies) {
        Vector2 position = body.getPosition();
            for (Fixture fixture : body.getFixtureList()) {
                Shape.Type shapeType = fixture.getShape().getType();
                switch (shapeType) {
                    case Circle:
                        sr.set(ShapeRenderer.ShapeType.Line);
                        sr.circle(position.x * mod, position.y * mod, fixture.getShape().getRadius() * mod);
                        break;
                    case Polygon:
                        PolygonShape polygon = (PolygonShape) fixture.getShape();
                        int vertexCount = polygon.getVertexCount();

                        Vector2 previousVertex = new Vector2();

                        for (int i = 0; i < vertexCount; i++) {
                            Vector2 currentVertex = new Vector2();
                            polygon.getVertex(i, currentVertex);
                            if (i == 0) {
                                polygon.getVertex(vertexCount - 1, previousVertex);
                            } else {
                                polygon.getVertex(i - 1, previousVertex);
                            }

                            previousVertex = body.getWorldPoint(previousVertex);
                            previousVertex.x *= mod;
                            previousVertex.y *= mod;
                            Vector2 prevVertCopy = previousVertex.cpy();
                            currentVertex = body.getWorldPoint(currentVertex);
                            currentVertex.x *= mod;
                            currentVertex.y *= mod;
                            Vector2 curVertCopy = previousVertex.cpy();
                            sr.line(curVertCopy, prevVertCopy);
                        }
                        sr.circle(position.x * mod, position.y * mod, 2);
                        break;
                }
            }
        }
        sr.end();
    }

    public void drawAll(Pixmap pixmap) {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    if (get(following, y).color != currentColor) {
                        break;
                    }
                    toIndex = following;
                }
                x = toIndex;
                if (element != null) {
                    pixmap.setColor(element.color);
                    pixmap.drawLine((int) element.pixelX, (int) element.pixelY, element.pixelX + toIndex, (int) element.pixelY);
                }
            }
        }
    }

    private float rectDrawWidth(int index) {
        return (index * pixelSizeModifier) + (pixelSizeModifier - 1);
    }

    public void stepProvidedRows(int minRow, int maxRow) {
        for (int y = minRow; y <= maxRow; y++) {
            Array<Element> row = getRow(y);
            for (int x : getShuffledXIndexes()) {
                Element element = row.get(x);
                if (element != null) {
                    element.step(this);
                }
            }
        }
    }

    public void stepProvidedColumns(int colIndex) {
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x : shuffledXIndexesForThreads.get(colIndex)) {
                try {
                    Element element = row.get(calculateIndexWithOffset(x));
                    if (element != null) {
                        element.step(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int calculateIndexWithOffset(int x) {
        if (x + threadedIndexOffset >= innerArraySize) {
            return (x + threadedIndexOffset) - (innerArraySize);
        } else {
            return x + threadedIndexOffset;
        }
    }

    public void addSpout(ElementType elementType, Vector3 touchPos, int brushSize) {
        spoutArray.add(new Spout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize));
    }

    public void spawnFromSpouts() {
        for (Spout spout : spoutArray) {
            spawnElementByMatrixWithBrush(spout.matrixX, spout.matrixY, spout.sourceElement, spout.brushSize);
        }
    }


    public int toMatrix(float pixelVal) {
        return toMatrix((int) pixelVal);
    }

    public int toMatrix(int pixelVal) {
        return pixelVal / pixelSizeModifier;
    }

    public boolean clearAll() {
        matrix = generateMatrix();
        spoutArray.clear();
        physicsElementActors.clear();
        boids.clear();
        return true;
    }

    public Element get(Vector3 location) {
        return get((int) location.x, (int) location.y);
    }

    public Element get(float x, float y) {
        return get((int) x, (int) y);
    }

    public Element get(int x, int y) {
        if (isWithinBounds(x, y)) {
            return matrix.get(y).get(x);
        } else {
            return null;
        }
    }

    public Array<Element> getRow(int index) {
        return matrix.get(index);
    }

    public boolean setElementAtIndex(int x, int y, Element element) {
        matrix.get(y).set(x, element);
        element.setCoordinatesByMatrix(x, y);
        return true;
    }

    public void spawnElementByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int localBrushSize) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrixWithBrush(matrixX, matrixY, elementType, localBrushSize);
    }

    public void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrix(matrixX, matrixY, elementType);
    }

    public void spawnElementByMatrixWithBrush(int matrixX, int matrixY, ElementType elementType, int localBrushSize) {
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                if (distance < halfBrush) {
                    spawnElementByMatrix(x, y, elementType);
                }
            }
        }
    }

    public Element spawnElementByMatrix(int matrixX, int matrixY, ElementType elementType) {
        if (isWithinBounds(matrixX, matrixY)) {
            Element currentElement = get(matrixX, matrixY);
            if (currentElement.getClass() != elementType.clazz && !(currentElement instanceof PlayerMeat)) {
                get(matrixX, matrixY).die(this);
                Element newElement = elementType.createElementByMatrix(matrixX, matrixY);
                setElementAtIndex(matrixX, matrixY, newElement);
                reportToChunkActive(newElement);
                return newElement;
            }
        }
        return null;
    }
    public boolean isWithinBounds(Vector2 vec) {
        return isWithinBounds((int) vec.x, (int) vec.y);
    }
    
public void spawnParticleByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int brushSize) {
spawnParticleByMatrixWithBrush(createFunctionInput(toMatrix(pixelX), toMatrix(pixelY), elementType, brushSize, null));
}

public void spawnParticleByMatrixWithBrush(FunctionInput input) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = input.getBrushSize()/2;
        ElementType elementType = input.getElementType();
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                if (distance < halfBrush) {
                    Vector3 velocity = generateRandomVelocityWithBounds(-200, 200);
                    spawnParticleByMatrix(x, y, elementType, velocity);
                }
            }
        }
    }

    private void spawnParticleByMatrix(int x, int y, ElementType elementType, Vector3 velocity) {
        if (get(x, y) instanceof EmptyCell) {
            Element newElement = ElementType.createParticleByMatrix(this, x, y, velocity, elementType);
            if (newElement != null) {
                reportToChunkActive(newElement);
            }
        }
    }
    
    public void particalizeByMatrixWithBrush(FunctionInput input) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = input.getBrushSize()/2;
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                if (distance < halfBrush) {
                    Vector3 velocity = generateRandomVelocityWithBounds(-300, 300);
                    particalizeByMatrix(x, y, velocity);
                }
            }
        }
    }

    public void particalizeByMatrix(int x, int y, Vector3 velocity) {
        Element element = get(x, y);
        if (element instanceof MovableSolid || element instanceof Liquid) {
            element.dieAndReplaceWithParticle(this, velocity);
        }
    }
    public boolean isWithinBounds(int matrixX, int matrixY) {
        return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
    }

    public static int distanceBetweenTwoPoints(int x1, int x2, int y1, int y2) {
        return (int) Math.ceil(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public boolean isWithinXBounds(int matrixX) {
        return matrixX >= 0 && matrixX < innerArraySize;
    }

    public boolean isWithinYBounds(int matrixY) {
        return matrixY >= 0 && matrixY < outerArraySize;
    }

    public List<Integer> getShuffledXIndexes() {
        return shuffledXIndexes;
    }

    public void reshuffleXIndexes() {
        Collections.shuffle(shuffledXIndexes);
    }

    private List<Integer> generateShuffledIndexes(int size) {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    public List<List<Integer>> generateShuffledIndexesForThreads(int threadCount) {
        int colSize = innerArraySize / threadCount;// + (innerArraySize % threadCount);
        List<List<Integer>> indexList = new ArrayList<>(threadCount);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= innerArraySize; i++) {
            if (i % colSize == 0) {
                Collections.shuffle(list);
                indexList.add(list);
                list = new ArrayList<>(colSize);
            }
            list.add(i - 1);
        }
        if (!indexList.contains(list)) {
            indexList.get(indexList.size() - 1).addAll(list);
        }
        shuffledXIndexesForThreads = indexList;
        return indexList;
    }

    public void reshuffleThreadXIndexes(int numThreads) {
        if (shuffledXIndexesForThreads.size() != numThreads) {
            generateShuffledIndexesForThreads(numThreads);
            return;
        }
        //shuffledXIndexesForThreads.forEach(Collections::shuffle);
    for(List<Integer> shuffledXIndexesForThreads : shuffledXIndexesForThreads){
            
            Collections.shuffle(shuffledXIndexesForThreads);
            
        }
    }

    public void iterateAndSpawnBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize) {

        int matrixX1 = toMatrix((int) pos1.x);
        int matrixY1 = toMatrix((int) pos1.y);
        int matrixX2 = toMatrix((int) pos2.x);
        int matrixY2 = toMatrix((int) pos2.y);

        if (pos1.epsilonEquals(pos2)) {
            spawnElementByMatrixWithBrush(matrixX1, matrixY1, elementType, brushSize);
            return;
        }

        int xDiff = matrixX1 - matrixX2;
        int yDiff = matrixY1 - matrixY2;
        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

        int xModifier = xDiff < 0 ? 1 : -1;
        int yModifier = yDiff < 0 ? 1 : -1;

        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);

        int smallerCount = 0;
        for (int i = 1; i <= upperBound; i++) {
            if (freq != 0 && i % freq == 0 && min != smallerCount) {
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
            int currentY = matrixY1 + (yIncrease * yModifier);
            int currentX = matrixX1 + (xIncrease * xModifier);
            if (isWithinBounds(currentX, currentY)) {
                spawnElementByMatrixWithBrush(currentX, currentY, elementType, brushSize);
            }
        }
    }

//    public void iterateAndHeatBetweenTwoPoints(Vector3 pos1, Vector3 pos2, int brushSize) {
//        int matrixX1 = toMatrix((int) pos1.x);
//        int matrixY1 = toMatrix((int) pos1.y);
//        int matrixX2 = toMatrix((int) pos2.x);
//        int matrixY2 = toMatrix((int) pos2.y);
//
//        if (pos1.epsilonEquals(pos2)) {
//            applyHeatByBrush((int) pos1.x, (int) pos1.y, brushSize);
//            return;
//        }
//
//        int xDiff = matrixX1 - matrixX2;
//        int yDiff = matrixY1 - matrixY2;
//        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);
//
//        int xModifier = xDiff < 0 ? 1 : -1;
//        int yModifier = yDiff < 0 ? 1 : -1;
//
//        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
//        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
//        int freq = (min == 0 || upperBound == 0) ? 0 : (upperBound / min);
//
//        int smallerCount = 0;
//        for (int i = 1; i <= upperBound; i++) {
//            if (freq != 0 && i % freq == 0 && min != smallerCount) {
//                smallerCount += 1;
//            }
//            int yIncrease, xIncrease;
//            if (xDiffIsLarger) {
//                xIncrease = i;
//                yIncrease = smallerCount;
//            } else {
//                yIncrease = i;
//                xIncrease = smallerCount;
//            }
//            int currentY = matrixY1 + (yIncrease * yModifier);
//            int currentX = matrixX1 + (xIncrease * xModifier);
//            if (isWithinBounds(currentX, currentY)) {
//                applyHeatByBrush(currentX, currentY, brushSize);
//            }
//        }
//    }
    
    public void iterateAndHeatBetweenTwoPoints(Vector3 pos1, Vector3 pos2, int brushSize) {
        
        int matrixX1 = toMatrix((int) pos1.x);
        int matrixY1 = toMatrix((int) pos1.y);
        int matrixX2 = toMatrix((int) pos2.x);
        int matrixY2 = toMatrix((int) pos2.y);

        // If the two points are the same no need to iterate. Just run the provided function
        if (pos1.epsilonEquals(pos2)) {
                applyHeatByBrush((int) pos1.x, (int) pos1.y, brushSize);
            return;
        }

        int xDiff = matrixX1 - matrixX2;
        int yDiff = matrixY1 - matrixY2;
        boolean xDiffIsLarger = Math.abs(xDiff) > Math.abs(yDiff);

        int xModifier = xDiff < 0 ? 1 : -1;
        int yModifier = yDiff < 0 ? 1 : -1;

        int upperBound = Math.max(Math.abs(xDiff), Math.abs(yDiff));
        int min = Math.min(Math.abs(xDiff), Math.abs(yDiff));
        float floatFreq = (min == 0 || upperBound == 0) ? 0 : ((float) min / upperBound);
        int freqThreshold = 0;
        float freqCounter = 0;

        int smallerCount = 0;
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
            int currentY = matrixY1 + (yIncrease * yModifier);
            int currentX = matrixX1 + (xIncrease * xModifier);
            if (isWithinBounds(currentX, currentY)) {
                applyHeatByBrush(currentX, currentY, brushSize);
            }
        }
    }


    public void applyHeatByBrush(int matrixX, int matrixY, int localBrushSize) {
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                Element element = get(x, y);
                if (element != null) element.receiveHeat(this, 5);
            }
        }
    }
    
    public void reportToChunkActive(Element element) {
    reportToChunkActive(element.getMatrixX(), element.getMatrixY());
    }

    public void reportToChunkActive(int x, int y) {
       if (useChunks && isWithinBounds(x, y)) {
          if (x % Chunk.size == 0) {
                Chunk chunk = getChunkForCoordinates(x - 1 , y);
                              if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (x % Chunk.size == Chunk.size - 1) {
                Chunk chunk = getChunkForCoordinates(x + 1 , y);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (y % Chunk.size == 0) {
                Chunk chunk = getChunkForCoordinates(x, y - 1);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            if (y % Chunk.size == Chunk.size - 1) {
                Chunk chunk = getChunkForCoordinates(x, y + 1);
                if (chunk != null) chunk.setShouldStepNextFrame(true);
            }
            getChunkForCoordinates(x, y).setShouldStepNextFrame(true);
        }
    }

    public boolean shouldElementInChunkStep(Element element) {
        return getChunkForElement(element).getShouldStep();
    }

    public Chunk getChunkForElement(Element element) {
 return getChunkForCoordinates(element.getMatrixX(), element.getMatrixY());
    }

    public Chunk getChunkForCoordinates(int x, int y) {
        if (isWithinBounds(x, y)) {
            int chunkY = y / Chunk.size;
            int chunkX = x / Chunk.size;
            return chunks.get(chunkY).get(chunkX);
        }
        return null;
        }

    public void resetChunks() {
        for (int r = 0; r < chunks.size; r++) {
            Array<Chunk> chunkRow = chunks.get(r);
            for (int c = 0; c < chunkRow.size; c++) {
                Chunk chunk = chunkRow.get(c);
                chunk.shiftShouldStepAndReset();
            }
        }
    }
    
    public FunctionInput createFunctionInput(int matrixX, int matrixY, ElementType elementType, int brushSize, Vector3 velocity) {
        return new FunctionInput(matrixX, matrixY, elementType, brushSize, velocity);
    }
    
    private Vector3 generateRandomVelocityWithBounds(int lowerX, int upperX, int lowerY, int upperY) {
        int x = ThreadLocalRandom.current().nextInt(lowerX, upperX);
        int y = ThreadLocalRandom.current().nextInt(lowerY, upperY);
        return new Vector3(x, y, 0);
    }

    public Vector3 generateRandomVelocityWithBounds(int lower, int upper) {
        return generateRandomVelocityWithBounds(lower, upper, lower, upper);
    }
    //fi类
    public static class FunctionInput {

        Map<String, Object> inputs = new HashMap<>();

        public static final String X = "x";
        public static final String Y = "y";
        public static final String ELEMENT_TYPE = "elementType";
        public static final String BRUSH_SIZE = "brushSize";
        public static final String VELOCITY = "velocity";

        public FunctionInput() {

        }

        public FunctionInput(int matrixX, int matrixY, int brushSize) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(BRUSH_SIZE, brushSize);
        }

        public FunctionInput(int matrixX, int matrixY, int brushSize, ElementType elementType) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(ELEMENT_TYPE, elementType);
        }

        public FunctionInput(int matrixX, int matrixY, ElementType elementType, int brushSize, Vector3 velocity) {
            inputs.put(X, matrixX);
            inputs.put(Y, matrixY);
            inputs.put(ELEMENT_TYPE, elementType);
            inputs.put(BRUSH_SIZE, brushSize);
            inputs.put(VELOCITY, velocity);
        }

        public void setInput(String key, Object value) {
            inputs.put(key, value);
        }

        public int getMatrixX() {
            return (int) inputs.get(X);
        }

        public int getMatrixY() {
            return (int) inputs.get(Y);
        }

        public ElementType getElementType() {
            return (ElementType) inputs.get(ELEMENT_TYPE);
        }

        public int getBrushSize() {
            return (int) inputs.get(BRUSH_SIZE);
        }

        public Vector3 getVelocity() {
            return (Vector3) inputs.get(VELOCITY);
        }
    }
    
    public void spawnRect(Vector3 mouseDownPos, Vector3 mouseUpPos, ElementType currentlySelectedElement, BodyDef.BodyType bodyType) {
        int mod = CellularAutomaton.box2dSizeModifier;
        int matrixMouseDownX = toMatrix(mouseDownPos.x);
        int matrixMouseDownY = toMatrix(mouseDownPos.y);
        int matrixMouseUpX = toMatrix(mouseUpPos.x);
        int matrixMouseUpY = toMatrix(mouseUpPos.y);
        Vector2 boxCenter = new Vector2((float) (matrixMouseDownX + matrixMouseUpX) / 2, (float) (matrixMouseDownY + matrixMouseUpY) / 2);
        List<Vector2> vertices = getRectVertices(matrixMouseDownX, matrixMouseUpX, matrixMouseDownY, matrixMouseUpY);

        // min max are matrix coords
        int minX = innerArraySize;
        int maxX = 0;
        int minY = outerArraySize;
        int maxY = 0;
        for (Vector2 point : vertices) {
            minX = Math.min((int) point.x, minX);
            maxX = Math.max((int) point.x, maxX);
            minY = Math.min((int) point.y, minY);
            maxY = Math.max((int) point.y, maxY);
        }
        Array<Array<Element>> elementList = new Array<>();
        int xDistance = maxX - minX;
        int yDistance = maxY - minY;
        ElementType type = currentlySelectedElement;
        for (int y = minY; y < minY + yDistance; y++) {
            Array<Element> row = new Array<>();
            elementList.add(row);
            for (int x = minX; x < minX + xDistance; x++) {
                Element element = spawnElementByMatrix(x, y, type);
                row.add(element);
            }
        }
        Body body = ShapeFactory.createPolygonFromElementArray(minX, minY, elementList, bodyType);
        if (body == null) return;
        PhysicsElementActor newActor = new PhysicsElementActor(body, elementList, minX, maxY);
        physicsElementActors.add(newActor);
    }

    public void stepPhysicsElementActors() {
        for (PhysicsElementActor physicsElementActor : physicsElementActors) {
            physicsElementActor.step(this);
        }
    }

    public void drawPhysicsElementActors(ShapeRenderer sr) {
        for (PhysicsElementActor physicsElementActor : physicsElementActors) {
            physicsElementActor.draw(sr);
        }
    }
    
        public boolean setElementAtSecondLocation(int x, int y, Element element) {
        matrix.get(y).set(x, element);
        element.setSecondaryCoordinatesByMatrix(x, y);
        return true;
    }
    
    public void destroyPhysicsElementActor(PhysicsElementActor physicsElementActor) {
        this.physicsElementActors.removeValue(physicsElementActor, true);
    }

    private List<Vector2> getRectVertices(int minX, int maxX, int minY, int maxY) {
        List<Vector2> verts = new ArrayList<>();
        verts.add(new Vector2(minX, minY));
        verts.add(new Vector2(minX, maxY));
        verts.add(new Vector2(maxX, maxY));
        verts.add(new Vector2(maxX, minY));
        return verts;
    }
    
    public void addExplosion(int radius, int strength, Element sourceElement) {
        explosionArray.add(new Explosion(this, radius, strength, sourceElement));
    }

    public void addExplosion(int radius, int strength, int matrixX, int matrixY) {
        explosionArray.add(new Explosion(this, radius, strength, matrixX, matrixY));
    }

    public void executeExplosions() {
        for (Explosion explosion : explosionArray) {
            explosion.enact();
        }
        explosionArray.clear();
    }
    
        public Array<Boid> getBoidNeighbors(int matrixX, int matrixY) {
        if (useBoidChunks) {
            return getChunkBoidNeighbors(matrixX, matrixY);
        } else {
            return getAllBoidNeighbors(matrixX, matrixY);
        }
    }

    public Array<Boid> getAllBoidNeighbors(int matrixX, int matrixY) {
        Array<Boid> allBoids = new Array<>(boids);
        Array<Boid> neighbors = new Array<>();
        for (Boid boid : allBoids) {
            int distance = distanceBetweenTwoPoints(matrixX, boid.getMatrixX(), matrixY, boid.getMatrixY());
            if (distance > 0 && distance <= Boid.neighborDistance) {
                neighbors.add(boid);
            }
        }
        return neighbors;
    }

    public void addBoid(Boid boid) {
        this.boids.add(boid);
    }

    public void spawnBoidsWithBrush(int matrixX, int matrixY, int brushSize) {
        int halfBrush = brushSize/2;
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                //if (brushType.equals(InputManager.BRUSHTYPE.CIRCLE)) {
                    int distance = distanceBetweenTwoPoints(matrixX, x, matrixY, y);
                    if (distance < halfBrush) {
                        Vector3 velocity = generateRandomVelocityWithBounds(-50, 50);
                        spawnBoid(x, y, velocity);
                    }
                //} else {
                   // Vector3 velocity = generateRandomVelocityWithBounds(-50, 50);
                  //  spawnBoid(x, y, velocity);
               // }
            }
        }
    }

    public void spawnBoid(int x, int y, Vector3 velocity) {
        ElementType.createBoidByMatrix(this, x, y, velocity);
    }
    
        public Chunk addBoidToChunk(Boid boid, Chunk currentChunk) {
        Chunk chunk = getChunkForCoordinates(boid.getMatrixX(), boid.getMatrixY());
        if (chunk == null || chunk == currentChunk) {
            return chunk;
        }
        chunk.addBoid(boid);
        if (currentChunk != null) {
            currentChunk.removeBoid(boid);
        }
        return chunk;
    }

    public void removeBoidFromChunk(Boid boid) {
        Chunk chunk = getChunkForCoordinates(boid.getMatrixX(), boid.getMatrixY());
        chunk.removeBoid(boid);
    }
    
        public Array<Boid> getChunkBoidNeighbors(int matrixX, int matrixY) {
        Set<Chunk> chunks = new HashSet<>();
        Chunk currentChunk = getChunkForCoordinates(matrixX, matrixY);
        chunks.add(currentChunk);
        boolean top = false;
        boolean right = false;
        boolean bottom = false;
        boolean left = false;
        if (matrixX % Chunk.size < Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY);
            if (chunk != null) {
                chunks.add(chunk);
            }
            left = true;
        }
        if (matrixX % Chunk.size > Chunk.size - Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY);
            if (chunk != null) {
                chunks.add(chunk);
            }
            right = true;
        }
        if (matrixY % Chunk.size < Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
            top = true;
        }
        if (matrixY % Chunk.size > Chunk.size - Boid.neighborDistance) {
            Chunk chunk = getChunkForCoordinates(matrixX, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
            bottom = true;
        }
        if (top && right) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (right && bottom) {
            Chunk chunk = getChunkForCoordinates(matrixX + Boid.neighborDistance, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (bottom && left) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY + Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        } else if (top && left) {
            Chunk chunk = getChunkForCoordinates(matrixX - Boid.neighborDistance, matrixY - Boid.neighborDistance);
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        List<Boid> allNeighbors = new ArrayList<>();
//            for (int i = 0; i < chunks.size(); i++) {
//                allNeighbors.addAll(chunks.iterator(i).getAllBoids());
//            }
            for (Chunk chunk : chunks) {
                allNeighbors.addAll(chunk.getAllBoids());
            }
            
            List<Boid> filteredNeighbors = new ArrayList<>();
            for (Boid boid : allNeighbors) {
                int distance = distanceBetweenTwoPoints(matrixX, boid.getMatrixX(), matrixY, boid.getMatrixY());
                if (distance > 0 && distance < Boid.neighborDistance){
                    filteredNeighbors.add(boid);
                }
            }
            List<Boid> subList = filteredNeighbors.subList(0, Math.min(Boid.maxNeighbors, filteredNeighbors.size()));
        Array<Boid> returnList = ListToArray(subList);
       // chunks.forEach(chunk -> allNeighbors.addAll(chunk.getAllBoids()));
//        List<Boid> filteredNeighbors = allNeighbors.stream().filter(boid -> {
//            int distance = distanceBetweenTwoPoints(matrixX, boid.getMatrixX(), matrixY, boid.getMatrixY());
//            return distance > 0 && distance < Boid.neighborDistance;
//        }).collect(Collectors.toList());
//        List<Boid> subList = filteredNeighbors.subList(0, Math.min(Boid.maxNeighbors, filteredNeighbors.size()));
//        Array<Boid> returnList = new Array<>();
//        subList.forEach(returnList::add);
        return returnList;
    }
    
    public static Array<Boid> ListToArray(List<Boid> v){
        Array<Boid> nv = new Array<Boid>();
        for (int i = 0; i < v.size(); i++) {           
            nv.add(v.get(i));
        }
        return nv;
    }

}
