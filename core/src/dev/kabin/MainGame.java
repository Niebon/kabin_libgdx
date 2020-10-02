package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.global.GlobalData;
import dev.kabin.ui.DevInterface;
import dev.kabin.utilities.eventhandlers.EventUtil;

import static dev.kabin.utilities.eventhandlers.EnumWithBoolHandler.logger;

public class MainGame extends ApplicationAdapter {

	float stateTime;
	SpriteBatch batch;
	Stage stage;
	Skin skin;

	@Override
	public void create() {
		stage = new Stage(new ScreenViewport());
		DevInterface.init(stage);
		InputMultiplexer imp = new InputMultiplexer();
		imp.setProcessors(GlobalData.getInputProcessor(), stage);
		Gdx.input.setInputProcessor(imp);
		logger.setLevel(GlobalData.getLogLevel());
		EventUtil.setInputOptions(EventUtil.InputOptions.getRegisterAll());
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.


		EntityGroupProvider.actionForEachEntityOrderedByGroup(e -> e.render(batch, stateTime));

		//bundle.renderFrameByIndex(0);
		//bundle.renderNextAnimationFrame(stateTime);
		//System.out.println(bundle.getCurrentImageAssetPath());

		// Drawing stage last ensures that it occurs before entities.
		stage.act(stateTime);
		stage.draw();
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

}
