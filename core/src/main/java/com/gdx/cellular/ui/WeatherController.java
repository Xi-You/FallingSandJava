package com.gdx.cellular.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.gdx.cellular.*;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class WeatherController extends VisWindow {

private final InputManager inputManager;

	public WeatherController (InputManager inputManager) {
		super("气象控制器");
        this.inputManager = inputManager;
		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();
		addVisWidgets();		

		setSize(200, 200);
		setResizable(true);
		setPosition(300, 300);
		addCloseButton();
		closeOnEscape();	
	}	

	private void addVisWidgets () {
		VisLabel label = new VisLabel("开启/关闭"); 
        label.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.weatherSystem.toggle();
                return true;
            }
        });
        VisTree tree = new VisTree();
		Node item1 = new Node(new VisLabel("气象模式"));
		item1.add(new Node(label));      
		item1.add(new Node(createOtherLabel()));        
      
		item1.setExpanded(true);
		tree.add(item1);
		add(tree).expand().fill();
        ScrollPane scrollPane = new ScrollPane(tree, VisUI.getSkin(), "list");
        add(scrollPane).expand().fill();
	}
	
	private VisLabel createOtherLabel() {
        VisLabel label = new VisLabel("设置为当前选中元素"); 
        label.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.setCurrentElementOnWeather();
                return true;
            }
        });
        return label;
    }
    
    static class Node extends Tree.Node {
		public Node (Actor actor) {
			super(actor);
		}
	}
}
