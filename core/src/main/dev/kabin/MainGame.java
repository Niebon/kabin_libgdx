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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.shaders.ShaderFactory;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.eventhandlers.*;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
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
    protected final Map<EntityGroup, ShaderProgram> shaderProgramMap = new EnumMap<>(EntityGroup.class);
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
    private CameraWrapper camera;
    private ImageMetadataPoolLibgdx imageAnalysisPool;
    private Stage stage;
    // Private data:
    private float scaleFactor = 1.0f;
    private TextureAtlas textureAtlas;
    private SpriteBatch spriteBatch;
    private ShaderProgram ambientShader;
    private ShaderProgram lightShader;

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


    protected WorldRepresentation<EntityGroup, EntityLibgdx> getWorldRepresentation() {
        return worldRepresentation;
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

        camera = new CameraWrapper(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        threadHandler.reload();
        Player.getInstance().ifPresent(p -> p.setHandleInput(true));


        {
            ambientShader = ShaderFactory.ambientShader();
            lightShader = ShaderFactory.lightSourceShader();


//            shaderProgramMap.put(EntityGroup.BACKGROUND, ShaderFactory.ambientShader());
//            shaderProgramMap.put(EntityGroup.BACKGROUND_LAYER_2, ShaderFactory.ambientShader());
//            shaderProgramMap.put(EntityGroup.SKY, null);
//            shaderProgramMap.put(EntityGroup.CLOUDS, null);
//            shaderProgramMap.put(EntityGroup.CLOUDS_LAYER_2, null);
            shaderProgramMap.put(EntityGroup.FOCAL_POINT, lightShader);
//            shaderProgramMap.put(EntityGroup.FOREGROUND, ShaderFactory.lightSourceShader());
            shaderProgramMap.put(EntityGroup.STATIC_BACKGROUND, ambientShader);
            shaderProgramMap.put(EntityGroup.GROUND, ambientShader);
        }

        //textureAtlas.getTextures().forEach(t -> t.setFilter(Texture.TextureFilter.Nearest , Texture.TextureFilter.Nearest ));


    }

    protected KeyEventUtil getKeyEventUtil() {
        return keyEventUtil;
    }

    protected MouseEventUtil getMouseEventUtil() {
        return mouseEventUtil;
    }

    protected TextureAtlas getTextureAtlasShaded() {
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
        final float timeSinceLastFrame = Gdx.graphics.getDeltaTime();

        ambientShader.bind();
        lightShader.bind();

        // Render physics
        if (worldRepresentation != null) {
            final var parameters = new PhysicsParametersImpl(worldRepresentation, keyEventUtil);

            PhysicsEngine.renderOutstandingFrames(timeSinceLastFrame, parameters, worldRepresentation::forEachEntityInCameraNeighborhood);
        }

        updateCamera(camera);


        spriteBatch.setProjectionMatrix(camera.getCamera().combined);

        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        // Render graphics
        if (worldRepresentation != null) {
            final ShaderProgram prg = shaderProgramMap.get(EntityGroup.FOCAL_POINT);
            prg.setUniformf("light_source", new Vector2(
                    Player.getInstance().map(Player::getX).orElse(0f) - getCameraX() + getCameraWrapper().getCamera().viewportWidth * 0.5f,
                    Player.getInstance().map(Player::getY).orElse(0f) - getCameraY() + getCameraWrapper().getCamera().viewportHeight * 0.5f + 50
            ));
            prg.setUniformf("r02", 100f);

            final GraphicsParametersImpl graphicsParameters = new GraphicsParametersImpl(spriteBatch,
                    camera.getCamera(),
                    consumer -> worldRepresentation.actionForEachEntityOrderedByType(consumer),
                    timeSinceLastFrame,
                    scaleFactor,
                    screenWidth,
                    screenHeight,
                    shaderProgramMap);
            worldRepresentation.forEachEntityInCameraNeighborhood(e ->
                    e.updateGraphics(graphicsParameters)
            );
        }

        //bundle.renderFrameByIndex(0);
        //bundle.renderNextAnimationFrame(stateTime);
        //System.out.println(bundle.getCurrentImageAssetPath());

        // Drawing stage last ensures that it occurs before dev.kabin.entities.
        stage.act(timeSinceLastFrame);


        stage.draw();


        //DebugUtil.renderEachCollisionPoint(worldRepresentation::isCollisionAt, camera.currentCameraBounds, scaleFactor);
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

    record GraphicsParametersImpl(@NotNull SpriteBatch batch,
                                  @NotNull Camera camera,
                                  @NotNull Consumer<@NotNull Consumer<@NotNull EntityLibgdx>> forEachEntityInCameraNeighborhood,
                                  float timeElapsedSinceLastFrame,
                                  float scale,
                                  float screenWidth,
                                  float screenHeight,
                                  Map<EntityGroup, ShaderProgram> shaders) implements GraphicsParametersLibgdx {


        @Override
        public float camX() {
            return camera.position.x;
        }

        @Override
        public float camY() {
            return camera.position.y;
        }

        @Override
        public @Nullable ShaderProgram shaderFor(EntityGroup group) {
            return shaders.get(group);
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
