package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.impl.Entity;
import dev.kabin.entities.impl.Player;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.eventhandlers.*;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class MainGame extends ApplicationAdapter {

	public static CameraWrapper camera;
	public static int screenWidth = 400;
	public static float scaleFactor = 1.0f;
	public static int screenHeight = 225;
	private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());
	protected WorldRepresentation worldRepresentation;
	public final KeyEventUtil keyEventUtil = new KeyEventUtil();
	public final MouseEventUtil mouseEventUtil = new MouseEventUtil(this::getWorldRepresentation);
	private final InputProcessor inputProcessor = new InputEventDistributor(mouseEventUtil, keyEventUtil);

	protected final ThreadHandler threadHandler = new ThreadHandler(this::getWorldRepresentation);
	protected TextureAtlas textureAtlas;
	private float stateTime = 0f;
	private SpriteBatch spriteBatch;

	protected WorldRepresentation getWorldRepresentation() {
		return worldRepresentation;
	}

	protected float getStateTime() {
		return stateTime;
	}

	protected void synchronizer(Runnable r) {
		threadHandler.synchronize(r);
	}

	@Override
	public void create() {
		textureAtlas = new TextureAtlas("textures.atlas");
		GlobalData.shapeRenderer = new ShapeRenderer();
		GlobalData.stage = new Stage(new ScreenViewport());
		GlobalData.userInterfaceBatch = new SpriteBatch();


		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		scaleFactor = (float) screenWidth / GlobalData.ART_WIDTH;

		InputMultiplexer imp = new InputMultiplexer();
		imp.setProcessors(inputProcessor, GlobalData.stage);
		Gdx.input.setInputProcessor(imp);
		logger.setLevel(GlobalData.getLogLevel());
		EventUtil.setInputOptions(
				EventUtil.InputOptions.registerAll(scaleFactor),
				keyEventUtil,
				mouseEventUtil,
				this::getWorldRepresentation
		);
		spriteBatch = new SpriteBatch();


		camera = new CameraWrapper(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		threadHandler.reload();
		Player.getInstance().ifPresent(p -> p.setHandleInput(true));
	}

	protected KeyEventUtil getKeyEventUtil() {
		return keyEventUtil;
	}

	protected MouseEventUtil getMouseEventUtil() {
		return mouseEventUtil;
	}

	protected TextureAtlas getTextureAtlas() {
		return textureAtlas;
	}

	/**
	 * Helper method to update the camera. Can be overridden by subclasses.
	 *
	 * @param cameraWrapper
	 */
	protected void updateCamera(CameraWrapper cameraWrapper){
		Player.getInstance().ifPresent(cameraWrapper::follow);
	}

	@Override
	public void render() {
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.


		// Render physics
		if (worldRepresentation != null) {
			final var parameters = new PhysicsParametersImpl(worldRepresentation, keyEventUtil);
			PhysicsEngine.renderOutstandingFrames(stateTime, parameters, worldRepresentation::forEachEntityInCameraNeighborhood);
		}

		updateCamera(camera);

		// Shading
		Pixmap p = new Pixmap(new byte[0], 0, 0);


		spriteBatch.setProjectionMatrix(camera.getCamera().combined);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.

		// Render graphics
		if (worldRepresentation != null) {
			worldRepresentation.forEachEntityInCameraNeighborhood(e ->
					e.updateGraphics(new GraphicsParametersImpl(spriteBatch,
							camera.getCamera(),
							worldRepresentation::forEachEntityInCameraNeighborhood,
							stateTime,
							scaleFactor,
							screenWidth,
							screenHeight)));
		}

		//bundle.renderFrameByIndex(0);
		//bundle.renderNextAnimationFrame(stateTime);
		//System.out.println(bundle.getCurrentImageAssetPath());

		// Drawing stage last ensures that it occurs before dev.kabin.entities.
		GlobalData.stage.act(stateTime);
		GlobalData.stage.draw();

		//DebugUtil.renderEachCollisionPoint(shapeRenderer, currentCameraBounds, scaleFactor);
		//DebugUtil.renderEachRoot(shapeRenderer, currentCameraBounds, scaleFactor);


	}

	@Override
	public void dispose() {
		GlobalData.userInterfaceBatch.dispose();
		spriteBatch.dispose();
	}

	static class PhysicsParametersImpl implements PhysicsParameters {

		@NotNull
		private final WorldRepresentation worldRepresentation;
		@NotNull
		private final KeyEventUtil keyEventUtil;

		PhysicsParametersImpl(@NotNull WorldRepresentation worldRepresentation,
							  @NotNull KeyEventUtil keyEventUtil) {
			this.worldRepresentation = worldRepresentation;
			this.keyEventUtil = keyEventUtil;
		}

		@Override
		public boolean isCollisionAt(int x, int y) {
			return worldRepresentation.isCollisionAt(x, y);
		}

		@Override
		public boolean isLadderAt(int x, int y) {
			return worldRepresentation.isLadderAt(x, y);
		}

		@Override
		public float getVectorFieldX(int x, int y) {
			return worldRepresentation.getVectorFieldX(x, y);
		}

		@Override
		public float getVectorFieldY(int x, int y) {
			return worldRepresentation.getVectorFieldY(x, y);
		}

		@Override
		public boolean isPressed(KeyCode keycode) {
			return keyEventUtil.isPressed(keycode);
		}

		@Override
		public float dt() {
			return PhysicsEngine.DT;
		}

		@Override
		public float meter() {
			return PhysicsEngine.METER * scaleFactor;
		}
	}

	static class GraphicsParametersImpl implements GraphicsParameters {

		@NotNull
		private final SpriteBatch spriteBatch;
		private final float stateTime, scale, screenWidth, screenHeight;
		@NotNull
		private final Camera camera;
		private final Consumer<Consumer<Entity>> forEachEntityInCameraNeighborhood;

		GraphicsParametersImpl(@NotNull SpriteBatch spriteBatch,
							   @NotNull Camera camera,
							   Consumer<Consumer<Entity>> forEachEntityInCameraNeighborhood, float stateTime,
							   float scale,
							   float screenWidth,
							   float screenHeight) {
			this.spriteBatch = spriteBatch;
			this.forEachEntityInCameraNeighborhood = forEachEntityInCameraNeighborhood;
			this.stateTime = stateTime;
			this.camera = camera;
			this.scale = scale;
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
		}

		@Override
		public @NotNull SpriteBatch getBatch() {
			return spriteBatch;
		}

		@Override
		public float getStateTime() {
			return stateTime;
		}

		@Override
		public float getScreenWidth() {
			return screenWidth;
		}

		@Override
		public float getScreenHeight() {
			return screenHeight;
		}

		@Override
		public float getCamX() {
			return camera.position.x;
		}

		@Override
		public float getCamY() {
			return camera.position.y;
		}

		@Override
		public float getScale() {
			return scale;
		}

		@Override
		public Consumer<Consumer<Entity>> forEachEntityInCameraNeighborhood() {
			return forEachEntityInCameraNeighborhood;
		}

	}


	public static class CameraWrapper {

		// Bounds
		public final MutableRectInt currentCameraBounds = MutableRectInt.centeredAt(0, 0, screenWidth, screenHeight);
		public final RectIntView currentCameraBoundsView = new RectIntView(currentCameraBounds);
		// Neighborhood
		public final MutableRectInt currentCameraNeighborhood = MutableRectInt.centeredAt(0, 0, 2 * screenWidth, 2 * screenHeight);
		public final RectIntView currentCameraNeighborhoodView = new RectIntView(currentCameraNeighborhood);
		@NotNull
		private final OrthographicCamera camera;
		private final WeightedAverage2D directionalPreSmoothening = new WeightedAverage2D(0.1f);
		private final WeightedAverage2D directionalFinalSmoothening = new WeightedAverage2D(0.005f);


		public CameraWrapper(@NotNull OrthographicCamera camera) {
			this.camera = camera;
		}

		public void setPos(float x, float y) {
			camera.position.set(x, y, camera.position.z);
			camera.update();
			// Find new camera position:
			currentCameraBounds.translate(
					Math.round(Functions.toIntDivideBy(x, scaleFactor) - currentCameraBounds.getCenterX()),
					Math.round(Functions.toIntDivideBy(y, scaleFactor) - currentCameraBounds.getCenterY())
			);
			currentCameraNeighborhood.translate(
					Math.round(Functions.toIntDivideBy(x, scaleFactor) - currentCameraNeighborhood.getCenterX()),
					Math.round(Functions.toIntDivideBy(y, scaleFactor) - currentCameraNeighborhood.getCenterY())
			);
		}

		@NotNull
		public OrthographicCamera getCamera() {
			return camera;
		}

		public void follow(Player player) {
			final float unit = 3 * player.getMaxPixelHeight() * scaleFactor;
			directionalPreSmoothening.appendSignalX((float) (Math.signum(player.getVx()) * unit));
			directionalPreSmoothening.appendSignalY((float) (Math.signum(player.getVy()) * unit + 0.5f * unit));
			directionalFinalSmoothening.appendSignalX(directionalPreSmoothening.x());
			directionalFinalSmoothening.appendSignalY(directionalPreSmoothening.y());
			setPos(player.getX() + directionalFinalSmoothening.x(),
					player.getY() + directionalFinalSmoothening.y()
			);
		}

		public RectIntView currentCameraBounds() {
			return currentCameraBoundsView;
		}

		public RectIntView getCameraNeighborhood() {
			return currentCameraNeighborhoodView;
		}
	}
}
