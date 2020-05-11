package com.gdx.cellular;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gdx.cellular.elements.ColorConstants;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.elements.EmptyCell;
import com.gdx.cellular.elements.liquid.Lava;
import com.gdx.cellular.spouts.ElementSpout;
import com.gdx.cellular.spouts.ParticleSpout;
import com.gdx.cellular.spouts.Spout;
import com.gdx.cellular.util.Chunk;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;



public class CellularMatrix {

    public int innerArraySize;
    public int outerArraySize;
    public int pixelSizeModifier;
    private final List<Integer> shuffledXIndexes;
    private List<List<Integer>> shuffledXIndexesForThreads;
    private int threadedIndexOffset = 0;

    private Array<Array<Element>> matrix;
    private Array<Array<Chunk>> chunks;
    public Array<Spout> spoutArray;

    public CellularMatrix(int width, int height, int pixelSizeModifier) {
        this.pixelSizeModifier = pixelSizeModifier;
        innerArraySize = toMatrix(width);
        outerArraySize = toMatrix(height);
        matrix = generateMatrix();
        chunks = generateChunks();
        shuffledXIndexes = generateShuffledIndexes(innerArraySize);

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
        sr.begin();
        sr.set(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < outerArraySize; y++) {
            Array<Element> row = getRow(y);
            for (int x = 0; x < row.size; x++) {
                Element element = row.get(x);
                Color currentColor = element.color;
                int toIndex = x;
                for (int following = x; following < row.size; following++) {
                    Element followingElement = get(following, y);
                    if (followingElement.color != currentColor) {
                        break;
                    }
                    toIndex = following;
                }
                x = toIndex;

                sr.setColor(element.color);
                sr.rect(element.pixelX, element.pixelY, rectDrawWidth(toIndex), pixelSizeModifier);

            }
        }
        sr.end();
        sr.setColor(ColorConstants.getColorForElementType(ElementType.LAVA));
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
                    pixmap.drawLine(element.pixelX, element.pixelY, element.pixelX + toIndex, element.pixelY);
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

    public void addSpout(ElementType elementType, Vector3 touchPos, int brushSize, boolean isParticle) {
        if (isParticle) {
            spoutArray.add(new ParticleSpout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize, this::spawnParticleByMatrixWithBrush));
        } else {
            spoutArray.add(new ElementSpout(elementType, toMatrix(touchPos.x), toMatrix(touchPos.y), brushSize, this::spawnElementByMatrixWithBrush));
        }

    }

    public void spawnFromSpouts() {
        for (Spout spout : spoutArray) {
            FunctionInput functionInput = spout.setFunctionInputs(new FunctionInput());
            spout.getFunction().accept(functionInput);
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
        spoutArray = new Array<>();
        return true;
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
        spawnElementByMatrixWithBrush(createFunctionInput(matrixX, matrixY, elementType, localBrushSize, null));
    }

    public void spawnElementByPixel(int pixelX, int pixelY, ElementType elementType) {
        int matrixX = toMatrix(pixelX);
        int matrixY = toMatrix(pixelY);
        spawnElementByMatrix(matrixX, matrixY, elementType);
    }

    public void spawnElementByMatrixWithBrush(FunctionInput input) {//int matrixX, int matrixY, ElementType elementType, int localBrushSize) {
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int localBrushSize = input.getBrushSize();
        ElementType elementType = input.getElementType();
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

    public void spawnElementByMatrix(int matrixX, int matrixY, ElementType elementType) {
        if (isWithinBounds(matrixX, matrixY) && get(matrixX, matrixY).getClass() != elementType.clazz) {
            Element newElement = elementType.createElementByMatrix(matrixX, matrixY);
            setElementAtIndex(matrixX, matrixY, elementType.createElementByMatrix(matrixX, matrixY));
            reportToChunkActive(newElement);
        }
    }

    public boolean isWithinBounds(int matrixX, int matrixY) {
        return matrixX >= 0 && matrixY >= 0 && matrixX < innerArraySize && matrixY < outerArraySize;
    }

    public int distanceBetweenTwoPoints(int x1, int x2, int y1, int y2) {
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
        shuffledXIndexesForThreads.forEach(Collections::shuffle);
    }

    public void applyHeatBetweenTwoPoints(Vector3 pos1, Vector3 pos2, int brushSize) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, null, brushSize, null, this:: applyHeatByBrush);
    }

    public void spawnElementBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, elementType, brushSize, null, this::spawnElementByMatrixWithBrush);
    }

    public void spawnParticleBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize, Vector3 velocity) {
        iterateAndApplyMethodBetweenTwoPoints(pos1, pos2, elementType, brushSize, velocity, this::spawnParticleByMatrixWithBrush);
    }

    public void iterateAndApplyMethodBetweenTwoPoints(Vector3 pos1, Vector3 pos2, ElementType elementType, int brushSize, Vector3 velocity, Consumer<FunctionInput> function) {

        int matrixX1 = toMatrix((int) pos1.x);
        int matrixY1 = toMatrix((int) pos1.y);
        int matrixX2 = toMatrix((int) pos2.x);
        int matrixY2 = toMatrix((int) pos2.y);

        if (pos1.epsilonEquals(pos2)) {
            FunctionInput input = createFunctionInput(matrixX1, matrixY1, elementType, brushSize, velocity);
            function.accept(input);
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
                FunctionInput input = createFunctionInput(currentX, currentY, elementType, brushSize, velocity);
                function.accept(input);
            }
        }
    }

    public void applyHeatByBrush(FunctionInput input) {
        int localBrushSize = input.getBrushSize();
        int matrixX = input.getMatrixX();
        int matrixY = input.getMatrixY();
        int halfBrush = (int) Math.floor(localBrushSize / 2);
        for (int x = matrixX - halfBrush; x <= matrixX + halfBrush; x++) {
            for (int y = matrixY - halfBrush; y <= matrixY + halfBrush; y++) {
                Element element = get(x, y);
                if (element != null) element.receiveHeat(5);
            }
        }
    }

    public FunctionInput createFunctionInput(int matrixX, int matrixY, ElementType elementType, int brushSize, Vector3 velocity) {
        return new FunctionInput(matrixX, matrixY, elementType, brushSize, velocity);
    }

    public void spawnParticleByPixelWithBrush(int pixelX, int pixelY, ElementType elementType, int brushSize, Vector3 velocity) {
        spawnParticleByMatrixWithBrush(createFunctionInput(toMatrix(pixelX), toMatrix(pixelY), elementType, brushSize, velocity));
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
                    Vector3 velocity = generateRandomVelocityWithBounds(-300, 300);
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

    private Vector3 generateRandomVelocityWithBounds(int lowerX, int upperX, int lowerY, int upperY) {
        int x = ThreadLocalRandom.current().nextInt(lowerX, upperX);
        int y = ThreadLocalRandom.current().nextInt(lowerY, upperY);
        return new Vector3(x, y, 0);
    }

    private Vector3 generateRandomVelocityWithBounds(int lower, int upper) {
        return generateRandomVelocityWithBounds(lower, upper, lower, upper);
    }

    public void reportToChunkActive(Element element) {
        getChunkForElement(element).setShouldStepNextFrame(true);
    }

    public boolean shouldElementInChunkStep(Element element) {
        return getChunkForElement(element).getShouldStep();
    }

    public Chunk getChunkForElement(Element element) {
        int chunkY = element.matrixY / Chunk.size;
        int chunkX = element.matrixX / Chunk.size;
        return chunks.get(chunkY).get(chunkX);
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
}
