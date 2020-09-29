package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.global.GlobalData;
import dev.kabin.ui.DevInterface;
import dev.kabin.utilities.eventhandlers.KeyEventUtil;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;

import static dev.kabin.utilities.eventhandlers.EnumWithBoolHandler.logger;

public class MainGame extends ApplicationAdapter {

	float stateTime;
	SpriteBatch batch;
	Stage stage;
	Skin skin;

	@Override
	public void create() {
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		stage = new Stage(new ScreenViewport());

		final TextButton button = new TextButton("Load image asset", skin, "default");
		button.setWidth(200);
		button.setHeight(50);
		button.addListener(new ClickListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				DevInterface.loadAsset();
				return true;
			}
		});


		Gdx.input.setInputProcessor(GlobalData.getInputProcessor());
		Gdx.input.setInputProcessor(stage);
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
		stage.addActor(button);
		batch = new SpriteBatch();


	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.
		stage.act(stateTime);
		stage.draw();

		EntityGroupProvider.actionForEachEntityOrderedByGroup(e -> e.render(batch, stateTime));

		//bundle.renderFrameByIndex(0);
		//bundle.renderNextAnimationFrame(stateTime);
		//System.out.println(bundle.getCurrentImageAssetPath());
	}

	@Override
	public void dispose () {
		batch.dispose();
	}

}
