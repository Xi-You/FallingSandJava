/*
 * Copyright 2014-2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gdx.cellular.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.gdx.cellular.InputManager;

public class BrushRegulator extends VisWindow {

    private final InputManager inputManager;
    
	public BrushRegulator (InputManager inputManager) {
		super("笔刷调节器");
        this.inputManager = inputManager;
		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();
	
		addVisWidgets();		

		setSize(200, 250);
		setResizable(true);
		setPosition(1154, 20);
		addCloseButton();
		closeOnEscape();	
	}

	private void addVisWidgets () {
		VisProgressBar progressbar = new VisProgressBar(0, 100, 1, true);
		final VisSlider slider = new VisSlider(inputManager.minBrushSize, inputManager.maxBrushSize, 1, true);
		VisSlider sliderDisabled = new VisSlider(0, 100, 1, true);

		progressbar.setValue(50);
        slider.addListener(new DragListener() {
                public void drag(InputEvent event, float x, float y, int pointer) {
                    inputManager.brushSize = (int) slider.getValue();                    
                }
            });
        slider.addListener(new InputListener() {
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    inputManager.brushSize = (int) slider.getValue();
                    return false;
                }
            });
		slider.setValue(inputManager.brushSize);
		sliderDisabled.setValue(50);
		sliderDisabled.setDisabled(true);

		VisTable progressbarTable = new VisTable(true);
		progressbarTable.add(progressbar);
		progressbarTable.add(slider);
		progressbarTable.add(sliderDisabled);

		add(progressbarTable);
	}
}
