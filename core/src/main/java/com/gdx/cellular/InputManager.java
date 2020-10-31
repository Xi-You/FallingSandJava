package com.gdx.cellular;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.Element;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.input.CreatorInputProcessor;
import com.gdx.cellular.input.MenuInputProcessor;
import com.gdx.cellular.util.TextInputHandler;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.gdx.cellular.box2d.ShapeFactory;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gdx.cellular.box2d.PhysicsElementActor;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.gdx.cellular.util.WeatherSystem;

public class InputManager {

    public final int maxBrushSize = 255;
    public final int minBrushSize = 1;
    private final int brushIncrements = 2;
    public MouseMode mouseMode = MouseMode.SPAWN;
    private Vector3 mouseDownPos = new Vector3();
    
    public Camera camera;
    public WeatherSystem weatherSystem;

    private final int maxThreads = 50;
    
    public int brushSize = 10;

    private Vector3 lastTouchPos = new Vector3();
    private Vector3 rectStartPos = new Vector3();
    private boolean touchedLastFrame = false;

    public ElementType currentlySelectedElement = ElementType.LAVA;
        public BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;

    private boolean paused = false;
    private boolean showDropDown = false;
    private Path savePath;// = Paths.get("save/");
    private String fileNameForLevel;
    private boolean readyToSave = false;
    private boolean readyToLoad = false;
    public boolean earClip = false;
    public boolean useBoidChunks = false;
   /* private final TextInputHandler saveLevelNameListener = new TextInputHandler(this, this::setFileNameForSave);
    private final TextInputHandler loadLevelNameListener = new TextInputHandler(this, this::setFileNameForLoad);
    private final Path path = Paths.get("save/");
    private String fileNameForLevel;
    private boolean readyToSave = false;
    private boolean readyToLoad = false;
    private boolean readyToOverride = false;
    private boolean showingOverrideConfirmation = false;*/

    public InputManager(OrthographicCamera camera) {
        this.camera = camera;
        this.weatherSystem = new WeatherSystem(ElementType.WATER, 2);
    }
    
    public MouseMode getMouseMode() {
        return this.mouseMode;
    }

public void setCurrentlySelectedElement(ElementType elementType) {
        this.currentlySelectedElement = elementType;
    }

public void setCurrentElementOnWeather() {
        this.weatherSystem.setElementType(this.currentlySelectedElement);
    }


    public void calculateNewBrushSize(int delta) {
        brushSize += delta;
        if (brushSize > maxBrushSize) brushSize = maxBrushSize;
        if (brushSize < minBrushSize) brushSize = minBrushSize;
    }

    public int adjustThreadCount(int numThreads) {
        int newThreads = numThreads;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            newThreads += numThreads == maxThreads ? 0 : 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            newThreads -= numThreads == 1 ? 0 : 1;
        }
        return newThreads;
    }

    public boolean toggleThreads(boolean toggleThreads) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            return !toggleThreads;
        } else {
            return toggleThreads;
        }
    }

public boolean toggleChunks(boolean toggleChunks) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            return !toggleChunks;
        } else {
            return toggleChunks;
        }
    }


    public void cycleMouseModes() {
        switch (mouseMode) {
            case SPAWN:
                this.mouseMode = MouseMode.HEAT;
                break;
            case HEAT:
                this.mouseMode = MouseMode.PARTICLE;
                break;
            case PARTICLE:
                this.mouseMode = MouseMode.PARTICALIZE;
                break;
            case PARTICALIZE:
                this.mouseMode = MouseMode.PHYSICSOBJ;
                break;
            case PHYSICSOBJ:
                this.mouseMode = MouseMode.RECTANGLE;
                break;
            case RECTANGLE:
                this.mouseMode = MouseMode.SPAWN;
        }
      
    }

    public void clearMatrix(CellularMatrix matrix) {
        matrix.clearAll();
        matrix.physicsElementActors = new Array<>();
    }

    public void placeSpout(CellularMatrix matrix, OrthographicCamera camera) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        if (mouseMode == MouseMode.SPAWN) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize);
        } else if (mouseMode == MouseMode.PARTICLE) {
            matrix.addSpout(currentlySelectedElement, touchPos, brushSize);
        }
    }
//    public void spawnElementByInput(CellularMatrix matrix, OrthographicCamera camera, ElementType currentlySelectedElement, World world) {
//    if (Gdx.input.isTouched()) {
//            Vector3 touchPos = new Vector3();
//            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
//            camera.unproject(touchPos);
//            if (mouseMode == MouseMode.SPAWN) {
//                if (touchedLastFrame) {
//                    matrix.iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
//                } else {
//                    matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
//                }
//                lastTouchPos = touchPos;
//                touchedLastFrame = true;
//            } else if (mouseMode == MouseMode.HEAT) {
//                if (touchedLastFrame) {
//                    matrix.iterateAndHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
//                } else {
//                    matrix.applyHeatByBrush(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
//                }
//                touchedLastFrame = true;
//            }
//        } else {
//            touchedLastFrame = false;
//        }
//    }
       public void setTouchedLastFrame(boolean touchedLastFrame) {
        this.touchedLastFrame = touchedLastFrame;
    }

    public void spawnElementByInput(CellularMatrix matrix, OrthographicCamera camera) {          
    Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (!touchedLastFrame) {
                mouseDownPos = touchPos.cpy();
            }
           switch (mouseMode) {
                case SPAWN:
                    if (touchedLastFrame) {
                       matrix.iterateAndSpawnBetweenTwoPoints(lastTouchPos, touchPos, currentlySelectedElement, brushSize);
                    } else {
                        matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                    case BOID:
                    matrix.spawnBoidsWithBrush(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
                    break;
                    case EXPLOSION:
                    if (touchedLastFrame) {
                        return;
                    } else {
                        matrix.addExplosion(brushSize, 3, matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y));
                    }
                case HEAT:
                    if (touchedLastFrame) {
                       matrix.iterateAndHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                    } else {
                        //CellularMatrix.FunctionInput input = new CellularMatrix.FunctionInput(matrix.toMatrix(touchPos.x), matrix.toMatrix(touchPos.y), brushSize);
                      //  matrix.applyHeatByBrush(input);
                       matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                case PARTICLE:
                    if (touchedLastFrame) {
                       matrix.iterateAndHeatBetweenTwoPoints(lastTouchPos, touchPos,/* currentlySelectedElement,*/ brushSize);
                    } else {
                        matrix.spawnParticleByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                case PARTICALIZE:
                    if (touchedLastFrame) {
                       matrix.iterateAndHeatBetweenTwoPoints(lastTouchPos, touchPos, brushSize);
                    } else {
                       matrix.spawnElementByPixelWithBrush((int) touchPos.x, (int) touchPos.y, currentlySelectedElement, brushSize);
                    }
                    break;
                case PHYSICSOBJ:
                    if (!touchedLastFrame) {
                        switch (currentlySelectedElement) {
                            case SAND:
                                spawnBox((int) touchPos.x, (int) touchPos.y, brushSize, matrix);
                                break;
                            case STONE:
                                ShapeFactory.createDefaultDynamicCircle((int) touchPos.x, (int) touchPos.y, brushSize / 2);
                                break;
                            case DIRT:
                                //ShapeFactory.createDynamicPolygonFromElementArray((int) touchPos.x, (int) touchPos.y, getRandomPolygonArray(), earClip);
                        }
                    }
                    break;
                    case RECTANGLE:
                    if (!touchedLastFrame) {
                        rectStartPos = new Vector3((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
                    }
                    break;
            }
            lastTouchPos = touchPos;
            touchedLastFrame = true;
    //    } else {
    //    boolean notTheSameLocation = lastTouchPos.x != mouseDownPos.x || lastTouchPos.y != mouseDownPos.y;
         //   if (touchedLastFrame && mouseMode == MouseMode.RECTANGLE && notTheSameLocation) {
         //       matrix.spawnRect(mouseDownPos, lastTouchPos, currentlySelectedElement);
      //      }
          //  touchedLastFrame = false;
       // }
    }

public void openMenu() {

    }

    public boolean getIsPaused() {
        boolean stepOneFrame = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            stepOneFrame = true;
        }
      
        return paused && !stepOneFrame;
    }

    public void setIsPaused(boolean isPaused) {
        this.paused = isPaused;
    }

public void togglePause() {
        paused = !paused;
    }
    
    public void drawRect() {
    }

 public void save(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && !readyToSave) {
            paused = true;
          //  Gdx.input.getTextInput(saveLevelNameListener, "Save Level", "File Name", "");
        }
        if (readyToSave) {
            Path newPath = savePath.resolve(fileNameForLevel + ".ser");
            if (!Files.exists(newPath)) {
                try {
                    Files.createDirectories(newPath.getParent());
                    Files.createFile(newPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            readyToSave = false;
            setIsPaused(false);
            try {
                try (Writer out = Files.newBufferedWriter(newPath, StandardCharsets.UTF_8)) {
                    String lastClass;
                    String currentClass;
                    int currentClassCount;
                    StringBuilder builder = new StringBuilder();
                    for (int r = 0; r < matrix.outerArraySize; r++) {
                        Array<Element> row = matrix.getRow(r);
                        lastClass = row.get(0).getClass().getSimpleName();
                        currentClassCount = 0;
                        for (int e = 0; e < row.size; e++) {
                            Element element = row.get(e);
                            currentClass = element.getClass().getSimpleName();
                            if (currentClass.equals(lastClass)) {
                                currentClassCount++;
                                lastClass = currentClass;
                                if (e == row.size - 1) {
                                    builder.append(currentClassCount);
                                    builder.append(",");
                                    builder.append(lastClass);
                                    builder.append(",");
                                }
                                continue;
                            }
                            builder.append(currentClassCount);
                            builder.append(",");
                            builder.append(lastClass);
                            builder.append(",");
                            currentClassCount = 1;
                            lastClass = currentClass;
                        }
                        builder.append("0,|,");
                    }
                    out.write(builder.toString());
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void load(CellularMatrix matrix) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            paused = true;
           // Gdx.input.getTextInput(loadLevelNameListener, "Load Level", "File Name", "");
        }
        if (readyToLoad) {
            try {
                readyToLoad = false;
                matrix.clearAll();
                setIsPaused(false);
                Path newPath = savePath.resolve(fileNameForLevel + ".ser");
                String level = Files.readAllLines(newPath, StandardCharsets.UTF_8).get(0);
                String[] splitLevel = level.split(",");
                Array<Element> row = matrix.getRow(0);
                int lastElementIndex = 0;
                int rowIndex = 0;
                for (int i = 0; i < splitLevel.length; i += 2) {
                    int count = Integer.parseInt((String) java.lang.reflect.Array.get(splitLevel, i));
                    String clazz = ((String) java.lang.reflect.Array.get(splitLevel, i + 1)).toUpperCase();
                    if (clazz.equals("|")) {
                        rowIndex++;
                        lastElementIndex = 0;
                        if (rowIndex > matrix.outerArraySize - 1) {
                            continue;
                        }
                        row = matrix.getRow(rowIndex);
                        continue;
                    }
                    for (int k = 0; k < count; k++) {
                        row.set(k + lastElementIndex, ElementType.valueOf(clazz).createElementByMatrix(k + lastElementIndex, rowIndex));
                    }
                    lastElementIndex += count;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean setFileNameForSave(String sane) {
        this.fileNameForLevel = sane;
        this.readyToSave = true;
        return true;
    }

    public boolean setFileNameForLoad(String sane) {
        this.fileNameForLevel = sane;
        this.readyToLoad = true;
        return true;
    }
    
    public void toggleEarClip() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            earClip = !earClip;
        }
    }
    
            public void spawnBox(int x, int y, int brushSize, CellularMatrix matrix) {
        int matrixX = matrix.toMatrix(x);
        int matrixY = matrix.toMatrix(y);
        Body body =  ShapeFactory.createDefaultDynamicBox(x, y, brushSize / 2);
        PolygonShape shape = (PolygonShape) body.getFixtureList().get(0).getShape();
        Vector2 point = new Vector2();
        shape.getVertex(0, point);
        Vector2 worldPoint1 = body.getWorldPoint(point).cpy();
        shape.getVertex(2, point);
        Vector2 worldPoint2 = body.getWorldPoint(point).cpy();

//        Array<Array<Element>> elementList = new Array<>();
//        for (int xIndex = matrix.toMatrix((int) worldPoint1.x); xIndex < matrix.toMatrix((int) (worldPoint1.x + (worldPoint2.x - worldPoint1.x))); xIndex++) {
//            Array<Element> row = new Array<>();
//            elementList.add(row);
//            for (int yIndex = matrix.toMatrix((int) worldPoint2.y); yIndex > matrix.toMatrix((int) (worldPoint2.y + (worldPoint1.y - worldPoint2.y))); yIndex--) {
//                Element element = matrix.spawnElementByMatrix(matrix.toMatrix(x), matrix.toMatrix(y), ElementType.STONE);
//                row.add(element);
//            }
//        }

//        PhysicsElementActor physicsElementActor = new PhysicsElementActor(body, elementList);
//        matrix.physicsElementActors.add(physicsElementActor);

    }

    public void spawnRect(CellularMatrix matrix, OrthographicCamera camera, BodyDef.BodyType bodyType) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        touchPos.set((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
        spawnRect(matrix, rectStartPos, touchPos, currentlySelectedElement, bodyType);
    }

   public void spawnRect(CellularMatrix matrix, Vector3 topLeft, Vector3 bottomRight, ElementType type, BodyDef.BodyType bodyType) {
        if (topLeft.x != bottomRight.x && topLeft.y != bottomRight.y) {            
            matrix.spawnRect(topLeft, bottomRight, type, bodyType);
        }
    }
    
        public void spawnPhysicsRect(CellularMatrix matrix, Vector3 touchPos) {
        touchPos.set((float) Math.floor(touchPos.x), (float) Math.floor(touchPos.y), 0);
        spawnPhysicsRect(matrix, rectStartPos, lastTouchPos, currentlySelectedElement, bodyType);
    }

    public void spawnPhysicsRect(CellularMatrix matrix, Vector3 topLeft, Vector3 bottomRight, ElementType type, BodyDef.BodyType bodyType) {
        if (topLeft.x != bottomRight.x && topLeft.y != bottomRight.y) {
            matrix.spawnRect(topLeft, bottomRight, type, bodyType);
        }
    }
    
    public void touchUpLMB(CellularMatrix matrix) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        switch (mouseMode) {
            case RECTANGLE:
                spawnPhysicsRect(matrix, touchPos);
                break;           
        }
    }
    
    public void clearBox2dActors() {
        ShapeFactory.clearAllActors();
    }
    
        public void toggleBoidChunks() {
        this.useBoidChunks = !this.useBoidChunks;
        System.out.println("UseBoidChunks: " + this.useBoidChunks);
    }


//    public void setReadyToOverride(boolean readyToOverride) {
//        this.readyToOverride = readyToOverride;
//    }
}
