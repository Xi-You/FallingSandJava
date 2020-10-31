package com.gdx.cellular.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.InputManager;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.box2d.*;

import static com.gdx.cellular.MouseMode.RECTANGLE;
import com.badlogic.gdx.math.Vector2;
import com.gdx.cellular.CellularAutomaton;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.math.Vector3;

public class CreatorInputProcessor implements InputProcessor {

    private final InputManager inputManager;
    private final OrthographicCamera camera;
    private final CellularMatrix matrix;
    
    private float box2dSizeModifier = 10;
    
    public CreatorInputProcessor(InputManager inputManager, OrthographicCamera camera, CellularMatrix matrix, CellularAutomaton cellularAutomaton) {
        this.inputManager = inputManager;
        this.camera = camera;
        this.matrix = matrix;      
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.EQUALS) {
            inputManager.calculateNewBrushSize(2);
        }
        if (keycode == Input.Keys.MINUS) {
            inputManager.calculateNewBrushSize(-2);
        }
        ElementType elementType = InputElement.getElementForKeycode(keycode);
        if (elementType != null) {
            inputManager.setCurrentlySelectedElement(elementType);
        }
        if (keycode == Input.Keys.SPACE) {
            inputManager.placeSpout(matrix, camera);
        }
        if (keycode == Input.Keys.C) {
            inputManager.clearMatrix(matrix);
            inputManager.clearBox2dActors();
            //inputManager.clearPhysicsElementObjects();
        }
        if (keycode == Input.Keys.P) {
            inputManager.togglePause();
        }
 //       if (keycode == Input.Keys.M) {
//            inputManager.cycleMouseModes();
    //    }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {    
        if (button == Input.Buttons.LEFT) {
//            if (inputManager.getMouseMode() == RECTANGLE) {
//                inputManager.drawRect(matrix, camera);
//            } else {
                inputManager.spawnElementByInput(matrix, camera);
               // inputManager.spawnRect(matrix, camera, BodyDef.BodyType.DynamicBody);
              // inputManager.spawnRect(matrix, new Vector3((camera.viewportWidth/2/box2dSizeModifier/8)*10, 200, 0), new Vector3((camera.viewportWidth/2/box2dSizeModifier - camera.viewportWidth/2/box2dSizeModifier/8)*10, 100, 0), ElementType.STONE, BodyDef.BodyType.DynamicBody);
//            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            inputManager.setTouchedLastFrame(false);
            inputManager.touchUpLMB(matrix);
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        inputManager.spawnElementByInput(matrix, camera);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
