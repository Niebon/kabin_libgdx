package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.shaders.LightSourceShader;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.eventhandlers.*;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MainGame extends ApplicationAdapter {

    // Statics
    public static int screenWidth = 400;
    public static int screenHeight = 225;

    // Protected data:
    protected final KeyEventUtil keyEventUtil = new KeyEventUtil();
    protected WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation;

    // Private fields:
    private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());
    private final MouseEventUtil mouseEventUtil = new MouseEventUtil(this::getWorldRepresentation, this::getCameraX, this::getCameraY, this::getScale);
    private final InputProcessor inputProcessor = new InputEventDistributor(mouseEventUtil, keyEventUtil);
    private final ThreadHandler threadHandler = new ThreadHandler(this::getWorldRepresentation, this::getCameraNeighborhood, this::getDevUI, this::isDeveloperMode);
    private final EventTriggerController eventTriggerController = new EventTriggerController(
            EventTriggerController.InputOptions.registerAll(),
            keyEventUtil,
            mouseEventUtil,
            this::getWorldRepresentation,
            Functions::getNull,
            this::getScale
    );


    // Private data:
    private float scaleFactor = 1.0f;
    private CameraWrapper camera;
    private ImageMetadataPoolLibgdx imageAnalysisPool;
    private Stage stage;


    public static ShaderProgram lightSourceShaders;


    /**
     * @return the scale factor for the pixel art from the native resolution 400 by 225.
     */
    public float getScale() {
        return scaleFactor;
    }

    // Protected methods:
    protected void setDeveloperUISupplier(Supplier<DeveloperUI> developerUISupplier) {
        eventTriggerController.setDeveloperUISupplier(developerUISupplier);
    }

    protected boolean isDeveloperMode() {
        return eventTriggerController.isDeveloperMode();
    }

    protected void setDeveloperMode(boolean developerMode) {
        eventTriggerController.setDeveloperMode(developerMode);
    }

    protected Stage getStage() {
        return stage;
    }

    private TextureAtlas textureAtlas;
    private float stateTime = 0f;
    private SpriteBatch spriteBatch;

    protected WorldRepresentation<EntityGroup, EntityLibgdx> getWorldRepresentation() {
        return worldRepresentation;
    }

    protected float getStateTime() {
        return stateTime;
    }

    protected void synchronizer(Runnable r) {
        threadHandler.synchronize(r);
    }

    private RectInt getCameraNeighborhood() {
        return camera.getCameraNeighborhood();
    }

    public CameraWrapper getCameraWrapper() {
        return camera;
    }


    @Override
    public void create() {
        textureAtlas = new TextureAtlas("textures.atlas");
        imageAnalysisPool = new ImageMetadataPoolLibgdx(textureAtlas);
        stage = new Stage();


        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        scaleFactor = (float) screenWidth / GlobalData.ART_WIDTH;

        {
            final InputMultiplexer imp = new InputMultiplexer();
            imp.setProcessors(inputProcessor, stage);
            Gdx.input.setInputProcessor(imp);
        }

        logger.setLevel(GlobalData.getLogLevel());
        eventTriggerController.setInputOptions(EventTriggerController.InputOptions.registerAll());
        spriteBatch = new SpriteBatch();


        lightSourceShaders = LightSourceShader.make();

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
     * @param cameraWrapper the camera wrapper that will be updated.
     */
    protected void updateCamera(CameraWrapper cameraWrapper) {
        Player.getInstance().ifPresent(cameraWrapper::follow);
    }

    public float getCameraX() {
        return camera.getCameraX();
    }

    public float getCameraY() {
        return camera.getCameraY();
    }

    @Nullable
    protected DeveloperUI getDevUI() {
        return null;
    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.
        spriteBatch.begin();
        spriteBatch.setShader(lightSourceShaders);


        // Render physics
        if (worldRepresentation != null) {
            final var parameters = new PhysicsParametersImpl(worldRepresentation, keyEventUtil);
            PhysicsEngine.renderOutstandingFrames(stateTime, parameters, worldRepresentation::forEachEntityInCameraNeighborhood);
        }

        updateCamera(camera);

        // Shading
        // Pixmap p = new Pixmap(new byte[0], 0, 0);


        spriteBatch.setProjectionMatrix(camera.getCamera().combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.

        // Render graphics
        if (worldRepresentation != null) {
            final GraphicsParametersImpl graphicsParameters = new GraphicsParametersImpl(spriteBatch,
                    camera.getCamera(),
                    consumer -> worldRepresentation.actionForEachEntityOrderedByType(consumer),
                    stateTime,
                    scaleFactor,
                    screenWidth,
                    screenHeight);
            worldRepresentation.forEachEntityInCameraNeighborhood(e ->
                    e.updateGraphics(graphicsParameters)
            );
        }

        spriteBatch.setShader(null);

        //bundle.renderFrameByIndex(0);
        //bundle.renderNextAnimationFrame(stateTime);
        //System.out.println(bundle.getCurrentImageAssetPath());

        // Drawing stage last ensures that it occurs before dev.kabin.entities.
        stage.act(stateTime);
        spriteBatch.end();
        stage.draw();

        //DebugUtil.renderEachCollisionPoint(shapeRenderer, currentCameraBounds, scaleFactor);
        //DebugUtil.renderEachRoot(shapeRenderer, currentCameraBounds, scaleFactor);

    }

    @Override
    public void dispose() {
        super.dispose();
        spriteBatch.dispose();
    }

    protected RectInt getCamBounds() {
        return getCameraWrapper().currentCameraBounds();
    }

    public ImageMetadataPoolLibgdx getImageAnalysisPool() {
        return imageAnalysisPool;
    }

    record GraphicsParametersImpl(@NotNull SpriteBatch spriteBatch,
                                  @NotNull Camera camera,
                                  Consumer<Consumer<EntityLibgdx>> forEachEntityInCameraNeighborhood,
                                  float stateTime, float scale, float screenWidth,
                                  float screenHeight) implements GraphicsParametersLibgdx {

        GraphicsParametersImpl(@NotNull SpriteBatch spriteBatch,
                               @NotNull Camera camera,
                               Consumer<Consumer<EntityLibgdx>> forEachEntityInCameraNeighborhood, float stateTime,
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
        public @NotNull
        SpriteBatch getBatch() {
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

    }

    class PhysicsParametersImpl implements PhysicsParameters {

        @NotNull
        private final WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation;
        @NotNull
        private final KeyEventUtil keyEventUtil;

        PhysicsParametersImpl(@NotNull WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation,
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


    public class CameraWrapper {

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

        public float getCameraX() {
            return camera.position.x;
        }

        public float getCameraY() {
            return camera.position.y;
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
