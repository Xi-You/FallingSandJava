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
import com.gdx.cellular.elements.*;
import com.gdx.cellular.*;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class ElementSelector extends VisWindow {

private final InputManager inputManager;

	public ElementSelector (InputManager inputManager) {
		super("元素选择器");
        this.inputManager = inputManager;
		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();

		if (CellularAutomaton.USE_VIS_WIDGETS)
			addVisWidgets();
		else
			addNormalWidgets();

		setSize(200, 300);
		setResizable(true);
		setPosition(774, 303);
		addCloseButton();
		closeOnEscape();	
	}

	private void addNormalWidgets () {
		Skin skin = VisUI.getSkin();

		Tree tree = new Tree(skin);
		Node item1 = new Node(new Label("gas", skin));
		Node item2 = new Node(new Label("liquid", skin));
		Node item3 = new Node(new Label("solid", skin));
		Node item4 = new Node(new Label("powder", skin));

		item1.add(new Node(new Label("item 1.1", skin)));
		item1.add(new Node(new Label("item 1.2", skin)));
		item1.add(new Node(new Label("item 1.3", skin)));

		item2.add(new Node(new Label("item 2.1", skin)));
		item2.add(new Node(new Label("item 2.2", skin)));
		item2.add(new Node(new Label("item 2.3", skin)));

		item3.add(new Node(new Label("item 3.1", skin)));
		item3.add(new Node(new Label("item 3.2", skin)));
		item3.add(new Node(new Label("item 3.3", skin)));

		item1.setExpanded(true);

		tree.add(item1);
		tree.add(item2);
		tree.add(item3);
        tree.add(item4);

		add(tree).expand().fill();
	}

	private void addVisWidgets () {
		VisTree tree = new VisTree();
        tree.addListener(new InputListener(){
                @Override
                public boolean scrolled (InputEvent event, float x, float y, int amount) {
                    return false;
                }
            });		
		Node item1 = new Node(new VisLabel("气体"));
		Node item2 = new Node(new VisLabel("液体"));
		Node item3 = new Node(new VisLabel("固体"));
		Node item4 = new Node(new VisLabel("粉末"));
		
		addElementLabels(item1,createElementLabels(ElementType.getGasses()));

        addElementLabels(item2,createElementLabels(ElementType.getLiquids()));
       		
        addElementLabels(item3,createElementLabels(ElementType.getImmovableSolids()));
        
        addElementLabels(item4,createElementLabels(ElementType.getMovableSolids()));
        
		item1.setExpanded(true);

		tree.add(item1);
		tree.add(item2);
		tree.add(item3);
        tree.add(item4);

		add(tree).expand().fill();
        ScrollPane scrollPane = new ScrollPane(tree, VisUI.getSkin(), "list");
        add(scrollPane).expand().fill();
	}

    private List<VisLabel> createElementLabels(List<ElementType> elements) {
    List<VisLabel> labels = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {

            labels.add(createElementLabel(elements.get(i)));
            
        }
        return labels;
    }

    private VisLabel createElementLabel(final ElementType elementType) {
        VisLabel label = new VisLabel(elementType.toString());
       // item.add(new Node(label);        
        label.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                inputManager.currentlySelectedElement = elementType;
                return true;
            }
        });
        return label;
    }
    
    private void addElementLabel(Node item, ElementType elementType) {        
        item.add(new Node(createElementLabel(elementType)));
    }
    
    private void addElementLabels(Node item, List<VisLabel> labels) {
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
