package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.kabin.global.GlobalData;
import dev.kabin.graphics.animation.AnimationBundle;
import dev.kabin.graphics.animation.AnimationBundleFactory;
import dev.kabin.graphics.animation.Animations;

import java.util.logging.Level;

import static dev.kabin.utilities.eventhandlers.EnumWithBoolHandler.logger;

public class MainGame extends ApplicationAdapter {

	float stateTime;
	AnimationBundle bundle;

	@Override
	public void create() {
		bundle = AnimationBundleFactory.loadFromAtlasPath("player");
		bundle.setScale(5);
		bundle.setX(0);
		bundle.setY(0);
		bundle.setWidth(32);
		bundle.setHeight(32);
		bundle.setCurrentAnimation(Animations.AnimationType.WALK_LEFT);
		Gdx.input.setInputProcessor(GlobalData.getInputProcessor());

		logger.setLevel(Level.INFO);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

		//bundle.renderFrameByIndex(0);
		bundle.renderNextAnimationFrame(stateTime);
	}
	
	@Override
	public void dispose () {
		bundle.dispose();
	}

}
