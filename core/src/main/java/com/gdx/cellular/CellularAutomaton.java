package com.gdx.cellular;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.cellular.box2d.ShapeFactory;
import com.gdx.cellular.elements.ElementType;
import com.gdx.cellular.input.InputProcessors;
import com.gdx.cellular.ui.*;
import com.gdx.cellular.util.GameManager;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.VisUI.SkinScale;
import com.kotcrab.vis.ui.test.manual.TestBusyBar;
import com.kotcrab.vis.ui.test.manual.TestButtonBar;
import com.kotcrab.vis.ui.test.manual.TestGenerateDisabledImage;
import com.kotcrab.vis.ui.test.manual.TestHighlightTextArea;
import com.kotcrab.vis.ui.test.manual.TestIssue131;
import com.kotcrab.vis.ui.test.manual.TestIssue326;
import com.kotcrab.vis.ui.test.manual.TestListView;
import com.kotcrab.vis.ui.test.manual.TestMultiSplitPane;
import com.kotcrab.vis.ui.test.manual.TestTabbedPane;
import com.kotcrab.vis.ui.test.manual.TestToasts;
import com.kotcrab.vis.ui.test.manual.WindowResizeEvent;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.overdrivr.tools.ContourToPolygons;
import com.overdrivr.tools.PNGtoBox2D;
import com.overdrivr.tools.b2Separator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import com.kotcrab.vis.ui.VisCHLoader;



public class CellularAutomaton extends ScreenAdapter {
	public static int screenWidth = Gdx.graphics.getWidth(); // 480;
	public static int screenHeight = Gdx.graphics.getHeight(); //800;
	public static int pixelSizeModifier = 2;
	public static int box2dSizeModifier = 10;
    public static Vector3 gravity = new Vector3(0f, -5f, 0f);
    public static BitSet stepped = new BitSet(1);

    private SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    private Pixmap pixmap;
    public CellularMatrix matrix;
    private OrthographicCamera camera;

    // private ElementType currentlySelectedElement = ElementType.LAVA;


    // private int brushSize = 5;

    private int numThreads = 20;
    private boolean useMultiThreading = true;

    private InputManager inputManager;

	private FPSLogger fpsLogger;
	public static int frameCount = 0;
    public boolean useChunks = true;
    public World b2dWorld;
	public Box2DDebugRenderer debugRenderer;
	public InputProcessors inputProcessors;
	public GameManager gameManager;
    public Viewport viewport;    

    public static final boolean USE_VIS_WIDGETS = true;
    public Stage uiStage;
	private MenuBar menuBar;
    public boolean usemenu = false;

    private Stage matrixStage;

	@Override
	public void show() {
	Gdx.gl.glEnable(GL20.GL_BLEND);
		fpsLogger = new FPSLogger();
		batch = new SpriteBatch();        

		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);
		camera.zoom = 1f;		

		pixmap = new Pixmap(0, 0, Pixmap.Format.fromGdx2DPixmapFormat(1));

        stepped.set(0, true);
		matrix = new CellularMatrix(screenWidth, screenHeight, pixelSizeModifier);
		matrix.generateShuffledIndexesForThreads(numThreads);                

		inputManager = new InputManager(camera);
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);


		b2dWorld = new World(new Vector2(0, -100), true);
		ShapeFactory.initialize(b2dWorld);
		debugRenderer = new Box2DDebugRenderer();
		//setUpBasicBodies();
		inputProcessors = new InputProcessors(inputManager, matrix, camera, this);
        this.gameManager = new GameManager(this);

        shapeRenderer = new ShapeRenderer();
        matrixStage = new Stage(viewport);
		matrixStage.addActor(new MatrixActor(shapeRenderer, matrix));
        createmenu();
        //shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);
		shapeRenderer.setAutoShapeType(true);
Gdx.input.setCatchBackKey(true);
		//this.gameManager.createPlayer(matrix.innerArraySize/2, matrix.outerArraySize/2);
        //  this.gameManager.createPlayer(8, 8);
      //  gameManager.createPlayer(matrix.innerArraySize/2, matrix.outerArraySize/2);
		//inputProcessors = new InputProcessors(inputManager, matrix, camera, gameManager);
        
	}

	@Override
	public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);        
        fpsLogger.log();
        stepped.flip(0);
        incrementFrameCount();

        if (Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            usemenu = !usemenu;
        }
        
        if (useChunks) {
			matrix.resetChunks();
		}        
        // Detect and act on input
       
        numThreads = inputManager.adjustThreadCount(numThreads);
        useMultiThreading = inputManager.toggleThreads(useMultiThreading);
        useChunks = inputManager.toggleChunks(useChunks);

		inputManager.openMenu();

        //inputManager.save(matrix);
		//inputManager.load(matrix);
		//inputManager.toggleEarClip();
		this.matrix.useBoidChunks = inputManager.useBoidChunks;

		matrix.reshuffleXIndexes();
		matrix.reshuffleThreadXIndexes(numThreads);
		matrix.calculateAndSetThreadedXIndexOffset();

		boolean isPaused = inputManager.getIsPaused();
		if (isPaused) {
		    matrix.useChunks = false;
			useChunks = false;
			matrixStage.draw();
			matrix.drawPhysicsElementActors(shapeRenderer);
			Array<Body> bodies = new Array<>();
			b2dWorld.getBodies(bodies);
			matrix.drawBox2d(shapeRenderer, bodies);
			debugRenderer.render(b2dWorld, camera.combined);
            if (usemenu) {
                Gdx.input.setInputProcessor(uiStage);                
                uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
                uiStage.draw();
                shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);                            
            } else {inputProcessors = new InputProcessors(inputManager, matrix, camera, this);}
            inputManager.weatherSystem.enact(this.matrix);
			return;
		}

		matrix.spawnFromSpouts();
		matrix.useChunks = useChunks;

		if (!useMultiThreading) {
			matrix.stepAndDrawAll(shapeRenderer);
		} else {
			matrix.reshuffleThreadXIndexes(numThreads);
			List<Thread> threads = new ArrayList<>(numThreads);

			for (int t = 0; t < numThreads; t++) {
				Thread newThread = new Thread(new ElementColumnStepper(matrix, t));
				threads.add(newThread);
			}
			if (stepped.get(0)) {
				startAndWaitOnOddThreads(threads);
				startAndWaitOnEvenThreads(threads);
			} else {
				startAndWaitOnEvenThreads(threads);
				startAndWaitOnOddThreads(threads);
			}

			//matrix.drawAll(shapeRenderer);
		}
		
		matrix.executeExplosions();

        b2dWorld.step(1 / 120f, 10, 6, 1);
        matrix.stepPhysicsElementActors();

		matrixStage.draw();		
		matrix.drawPhysicsElementActors(shapeRenderer);

		Array<Body> bodies = new Array<>();
		b2dWorld.getBodies(bodies);
		matrix.drawBox2d(shapeRenderer, bodies);       
        debugRenderer.render(b2dWorld, camera.combined);     

        
        
        if (usemenu) {
            Gdx.input.setInputProcessor(uiStage);
            matrixStage.draw();		
            uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            uiStage.draw();
            shapeRenderer.setProjectionMatrix(uiStage.getCamera().combined);            
            matrix.drawPhysicsElementActors(shapeRenderer);
            b2dWorld.getBodies(bodies);
            matrix.drawBox2d(shapeRenderer, bodies);
			debugRenderer.render(b2dWorld, camera.combined);
        } else {inputProcessors = new InputProcessors(inputManager, matrix, camera, this);}
        inputManager.weatherSystem.enact(this.matrix);
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
	}

    @Override
    public void resize(int width, int height) {
        if (width == 0 && height == 0) return; //see https://github.com/libgdx/libgdx/issues/3673#issuecomment-177606278
        uiStage.getViewport().update(width, height, true);
        matrixStage.getViewport().update(width, height, true);
//        PopupMenu.removeEveryMenu(uiStage);
//        WindowResizeEvent resizeEvent = new WindowResizeEvent();
//        for (Actor actor : uiStage.getActors()) {
//            actor.fire(resizeEvent);
//        }
	}

	private void incrementFrameCount() {
		frameCount = frameCount == 3 ? 0 : frameCount + 1;
	}


    public void createmenu() {
        VisCHLoader.load();

        uiStage = new Stage(viewport);
        final Table root = new Table();
        root.setFillParent(true);
        uiStage.addActor(root);

        // Gdx.input.setInputProcessor(uiStage);

        menuBar = new MenuBar();
        menuBar.setMenuListener(new MenuBar.MenuBarListener() {
                @Override
                public void menuOpened(Menu menu) {
                    System.out.println("Opened menu: " + menu.getTitle());
                }

                @Override
                public void menuClosed(Menu menu) {
                    System.out.println("Closed menu: " + menu.getTitle());
                }
            });
        root.add(menuBar.getTable()).expandX().fillX().row();
        root.add().expand().fill();

        createMenus();

//        uiStage.addActor(new TestCollapsible());
//        uiStage.addActor(new TestColorPicker());
//        if (Gdx.app.getType() == ApplicationType.Desktop) uiStage.addActor(new TestFileChooser());
//        uiStage.addActor(new TestWindow());
//        uiStage.addActor(new TestSplitPane());
//        uiStage.addActor(new TestTextAreaAndScroll());
       // uiStage.addActor(new ElementSelector(inputManager));       
      //  uiStage.addActor(new MouseModeSelector(inputManager));
   //     uiStage.addActor(new WeatherController(inputManager));
//        uiStage.addActor(new TestVertical());
//        uiStage.addActor(new TestFormValidator());
//        uiStage.addActor(new TestDialogs());
    //    uiStage.addActor(new BrushRegulator(inputManager));
//        uiStage.addActor(new TestBuilders());
//      stage.addActor(new TestTabbedPane());
//      stage.addActor(new TestFlowGroup());
//      stage.addActor(new TestButtonBar());
//      stage.addActor(new TestListView());
//      stage.addActor(new TestToasts(stage));
//      stage.addActor(new TestHighlightTextArea());
//      stage.addActor(new TestBusyBar());
//      stage.addActor(new TestMultiSplitPane());

//        uiStage.addListener(new InputListener() {
//                boolean debug = false;
//
//                @Override
//                public boolean keyDown (InputEvent event, int keycode) {
//                    if (keycode == Keys.F12) {
//                        debug = !debug;
//                        root.setDebug(debug, true);
//                        for (Actor actor : stage.getActors()) {
//                            if (actor instanceof Group) {
//                                Group group = (Group) actor;
//                                group.setDebug(debug, true);
//                            }
//                        }
//                        return true;
//                    }
//
//                    return false;
//                }
//            });
	}  

    private void createMenus() {
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu windowMenu = new Menu("Window");
        Menu helpMenu = new Menu("Help");
        Menu debugMenu = new Menu("调试");

        fileMenu.addItem(createTestsMenu());
        fileMenu.addItem(new MenuItem("menuitem #1"));
        fileMenu.addItem(new MenuItem("menuitem #2").setShortcut("f1"));
        fileMenu.addItem(new MenuItem("menuitem #3").setShortcut("f2"));
        fileMenu.addItem(new MenuItem("menuitem #4").setShortcut("alt + f4"));

        MenuItem subMenuItem = new MenuItem("submenu #1");
        subMenuItem.setShortcut("alt + insert");
        subMenuItem.setSubMenu(createSubMenu());
        fileMenu.addItem(subMenuItem);

        MenuItem subMenuItem2 = new MenuItem("submenu #2");
        subMenuItem2.setSubMenu(createSubMenu());
        fileMenu.addItem(subMenuItem2);

        MenuItem subMenuItem3 = new MenuItem("submenu disabled");
        subMenuItem3.setDisabled(true);
        subMenuItem3.setSubMenu(createSubMenu());
        fileMenu.addItem(subMenuItem3);

        // ---

        editMenu.addItem(new MenuItem("menuitem #5"));
        editMenu.addItem(new MenuItem("menuitem #6"));
        editMenu.addSeparator();
        editMenu.addItem(new MenuItem("menuitem #7"));
        editMenu.addItem(new MenuItem("menuitem #8"));
        editMenu.addItem(createDoubleNestedMenu());

        MenuItem disabledItem = new MenuItem("disabled menuitem");
        disabledItem.setDisabled(true);
        MenuItem disabledItem2 = new MenuItem("disabled menuitem shortcut").setShortcut("alt + f4");
        disabledItem2.setDisabled(true);

        editMenu.addItem(disabledItem);
        editMenu.addItem(disabledItem2);

        windowMenu.addItem(new MenuItem("menuitem #9"));
        windowMenu.addItem(new MenuItem("menuitem #10"));
        windowMenu.addItem(new MenuItem("menuitem #11"));
        windowMenu.addSeparator();
        windowMenu.addItem(new MenuItem("menuitem #12"));

        helpMenu.addItem(new MenuItem("about", new ChangeListener() {
                                 @Override
                                 public void changed(ChangeEvent event, Actor actor) {
                                     Dialogs.showOKDialog(uiStage, "about", "visui version: " + VisUI.VERSION);
                                 }
                             }));
                             
        debugMenu.addItem(new MenuItem("显示/关闭区块划分", new ChangeListener() {
                                 @Override
                                 public void changed(ChangeEvent event, Actor actor) {
                                     matrix.debugChunks = !matrix.debugChunks;
                                 }
                             }));
                             debugMenu.addItem(createDebugMenu(inputManager));

        menuBar.addMenu(fileMenu);
        menuBar.addMenu(editMenu);
        menuBar.addMenu(windowMenu);
        menuBar.addMenu(helpMenu);
        menuBar.addMenu(debugMenu);
    }

    private MenuItem createDoubleNestedMenu() {
        MenuItem doubleNestedMenuItem = new MenuItem("submenu nested x2");
        doubleNestedMenuItem.setSubMenu(createSubMenu());

        PopupMenu nestedMenu = new PopupMenu();
        nestedMenu.addItem(doubleNestedMenuItem);
        nestedMenu.addItem(new MenuItem("single nested"));

        MenuItem menuItem = new MenuItem("submenu nested");
        menuItem.setSubMenu(nestedMenu);
        return menuItem;
    }

    private MenuItem createTestsMenu() {
        MenuItem item = new MenuItem("start test");

        PopupMenu menu = new PopupMenu();
        menu.addItem(new MenuItem("tabbed pane", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestTabbedPane(false));
                             }
                         }));
        menu.addItem(new MenuItem("tabbed pane (vertical)", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestTabbedPane(true));
                             }
                         }));        
        menu.addItem(new MenuItem("button bar", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestButtonBar());
                             }
                         }));
        menu.addItem(new MenuItem("list view", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestListView());
                             }
                         }));
        menu.addItem(new MenuItem("toasts", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestToasts(uiStage));
                             }
                         }));
        menu.addItem(new MenuItem("highlight textarea", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestHighlightTextArea());
                             }
                         }));
        menu.addItem(new MenuItem("busybar", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestBusyBar());
                             }
                         }));
        menu.addItem(new MenuItem("multisplitpane", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestMultiSplitPane());
                             }
                         }));
        menu.addItem(new MenuItem("generate disabled image", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestGenerateDisabledImage());
                             }
                         }));
        menu.addSeparator();
        menu.addItem(new MenuItem("test issue #131", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestIssue131());
                             }
                         }));
        menu.addItem(new MenuItem("test issue #326", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new TestIssue326());
                             }
                         }));

        item.setSubMenu(menu);
        return item;
    }

    private MenuItem createDebugMenu(final InputManager inputManager) {
        MenuItem item = new MenuItem("控制台");

        PopupMenu menu = new PopupMenu();        
        menu.addItem(new MenuItem("元素选择器", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new ElementSelector(inputManager));  
                             }
                         }));
        menu.addItem(new MenuItem("鼠标模式", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                         uiStage.addActor(new MouseModeSelector(inputManager));
                             }
                         }));        
        menu.addItem(new MenuItem("气象控制", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                               uiStage.addActor(new WeatherController(inputManager));
                             }
                         }));
        menu.addItem(new MenuItem("笔刷调节", new ChangeListener() {
                             @Override
                             public void changed(ChangeEvent event, Actor actor) {
                                 uiStage.addActor(new BrushRegulator(inputManager));
                             }
                         }));

        item.setSubMenu(menu);
        return item;
    }

    private PopupMenu createSubMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addItem(new MenuItem("submenuitem #1"));
        menu.addItem(new MenuItem("submenuitem #2"));
        menu.addSeparator();
        menu.addItem(new MenuItem("submenuitem #3"));
        menu.addItem(new MenuItem("submenuitem #4"));
        return menu;
	}

    private void setUpBasicBodies() {
		BodyDef groundBodyDef = new BodyDef();

//		List<Vector2> verts = new ArrayList<>();
		inputManager.spawnRect(matrix, new Vector3((camera.viewportWidth / 2 / box2dSizeModifier / 8) * 10, 150, 0), new Vector3((camera.viewportWidth / 2 / box2dSizeModifier - camera.viewportWidth / 2 / box2dSizeModifier / 8) * 20, 50, 0), ElementType.STONE, BodyDef.BodyType.StaticBody);
//		ShapeFactory.createStaticRect(new Vector3(camera.viewportWidth/2/box2dSizeModifier, 10, 0), verts);

//		groundBodyDef.position.set(new Vector2(camera.viewportWidth/2/box2dSizeModifier, 10));
//
//
//		Body groundBody = b2dWorld.createBody(groundBodyDef);
//
//
//		PolygonShape groundBox = new PolygonShape();
//
//		groundBox.setAsBox(camera.viewportWidth/3/box2dSizeModifier, 5.0f);
//
//		groundBody.createFixture(groundBox, 0.0f);
//
//		groundBox.dispose();
	}

    private void setBasicBodies() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal("elementtextures/Player0.png"));
        PNGtoBox2D ptb = new PNGtoBox2D();
        b2Separator bs = new b2Separator();
        ContourToPolygons tr = new ContourToPolygons();
        Array<Vector2> simplified_contour = ptb.marchingSquares(pixmap);
        simplified_contour.reverse();
        FloatArray array = new FloatArray();
        for (int i = 1 ; i < simplified_contour.size ; i++) {
            array.add(simplified_contour.get(i).x);
            array.add(simplified_contour.get(i).y);
        }
		BodyDef groundBodyDef = new BodyDef();

		groundBodyDef.position.set(new Vector2(camera.viewportWidth / 2 / box2dSizeModifier, 10));


		Body groundBody = b2dWorld.createBody(groundBodyDef);
        FixtureDef fdef = new FixtureDef();
        fdef.density = 1;
        // bs.separate(groundBody,fdef,
        tr.BuildShape(groundBody, fdef, array);
//		PolygonShape groundBox = new PolygonShape();
//
//		groundBox.setAsBox(camera.viewportWidth / 3 / box2dSizeModifier, 5.0f);
//
//		groundBody.createFixture(groundBox, 0.0f);
//
//		groundBox.dispose();
	}

	private void startAndWaitOnEvenThreads(List<Thread> threads) {
		try {
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 == 0) {
					threads.get(t).start();
                }
			}
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 == 0) {
					threads.get(t).join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startAndWaitOnOddThreads(List<Thread> threads) {
		try {
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 != 0) {
					threads.get(t).start();
                }
			}
			for (int t = 0; t < threads.size(); t++) {
				if (t % 2 == 0) {
					threads.get(t).join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    public static Vector2 unproject(Vector2 pos, int sw, int sh) {
        Viewport v = new ExtendViewport(sw, sh);
        return  v.unproject(pos);
    } 

    @Override
	public void dispose() {
        VisUI.dispose();
		uiStage.dispose();
		batch.dispose();
		shapeRenderer.dispose();
        VisCHLoader.dispose(true);
	}



}
