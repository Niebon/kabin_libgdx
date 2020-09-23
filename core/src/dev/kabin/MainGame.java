package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.global.GlobalData;
import dev.kabin.graphics.animation.AnimationBundle;
import dev.kabin.utilities.eventhandlers.KeyEventUtil;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;

import static dev.kabin.utilities.eventhandlers.EnumWithBoolHandler.logger;

public class MainGame extends ApplicationAdapter {

	float stateTime;
	AnimationBundle bundle;

	@Override
	public void create() {

		Gdx.input.setInputProcessor(GlobalData.getInputProcessor());
		logger.setLevel(GlobalData.getLogLevel());
		MouseEventUtil.getInstance().addListener(
				MouseEventUtil.MouseButton.LEFT, true, () -> {
					if (KeyEventUtil.isShiftDown()) {
						float x = MouseEventUtil.getMouseX();
						float y = MouseEventUtil.getMouseY();
						System.out.println(x + "," + y);
						Entity e = EntityFactory.EntityType.PLAYER.getMouseClickConstructor().construct(x, y, "player", 1f);
						EntityGroupProvider.registerEntity(e);
					}
				}
		);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.


		EntityGroupProvider.actionForEachEntityOrderedByGroup(e -> e.render(stateTime));

		//bundle.renderFrameByIndex(0);
		//bundle.renderNextAnimationFrame(stateTime);
		//System.out.println(bundle.getCurrentImageAssetPath());
	}

	@Override
	public void dispose () {
		bundle.dispose();
	}

}
