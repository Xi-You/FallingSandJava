package com.gdx.cellular;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Screen;

public class Launcher extends Game {

	
    
	@Override
	public void create() {
		
		setScreen(new CellularAutomaton());
				
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause(){
		super.pause();
	}

	@Override
	public void resume(){
		super.resume();
	}
	
}
