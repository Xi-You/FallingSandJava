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

public class MouseModeSelector extends VisWindow {

private final InputManager inputManager;

	public MouseModeSelector (InputManager inputManager) {
		super("鼠标生成模式选择器");
        this.inputManager = inputManager;
		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();
		addVisWidgets();		

		setSize(200, 200);
		setResizable(true);
		setPosition(600, 300);
		addCloseButton();
		closeOnEscape();	
	}	

	private void addVisWidgets () {
		VisTree tree = new VisTree();
		Node item1 = new Node(new VisLabel("生成模式"));
		Node item2 = new Node(new VisLabel("刚体类型"));
		
		addMouseModeLabels(item1,createMouseModeLabels(MouseMode.values()));
        addMouseModeLabels(item2,createMouseModeLabels(BodyDef.BodyType.values()));
      
		item1.setExpanded(true);

		tree.add(item1);
		tree.add(item2);
		
		add(tree).expand().fill();
        ScrollPane scrollPane = new ScrollPane(tree, VisUI.getSkin(), "list");
        add(scrollPane).expand().fill();
	}

    private List<VisLabel> createMouseModeLabels(MouseMode[] MouseModes) {
    List<VisLabel> labels = new ArrayList<>();
        for (int i = 0; i < MouseModes.length; i++) {

            labels.add(createMouseModeLabel(MouseModes[i]));
            
        }
        return labels;
    }

    private List<VisLabel> createMouseModeLabels(BodyDef.BodyType[] BodyTypes) {
    List<VisLabel> labels = new ArrayList<>();
        for (int i = 0; i < BodyTypes.length; i++) {

            labels.add(createBodyTypeLabel(BodyTypes[i]));
            
        }
        return labels;
    }

    private VisLabel createMouseModeLabel(final MouseMode MouseModeType) {
        VisLabel label = new VisLabel(MouseModeType.toString()); 
        label.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.mouseMode = MouseModeType;
                return true;
            }
        });
        return label;
    }
    
        private VisLabel createBodyTypeLabel(final BodyDef.BodyType bodyType) {
        VisLabel label = new VisLabel(bodyType.toString());  
        label.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.bodyType = bodyType;
                return true;
            }
        });
        return label;
    }
    
    private void addMouseModeLabel(Node item, MouseMode MouseModeType) {        
        item.add(new Node(createMouseModeLabel(MouseModeType)));
    }
    
    private void addMouseModeLabels(Node item, List<VisLabel> labels) {
        for (int i = 0; i < labels.size(); i++) {
            
            item.add(new Node(labels.get(i)));
            
        }        
    }

	static class Node extends Tree.Node {
		public Node (Actor actor) {
			super(actor);
		}
	}
}
