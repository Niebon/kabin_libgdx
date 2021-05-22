package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.EntityPhysicsEngine;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.libgdx.EventTriggerController;
import dev.kabin.libgdx.InputEventDistributor;
import dev.kabin.shaders.LightSourceData;
import dev.kabin.shaders.LightSourceShaderBinder;
import dev.kabin.shaders.ShaderFactory;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.Functions;
import dev.kabin.util.events.EnumWithBoolHandler;
import dev.kabin.util.events.KeyEventUtil;
import dev.kabin.util.events.MouseEventUtil;
import dev.kabin.util.shapes.primitive.RectInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MainGame extends ApplicationAdapter {

    // Fields
    private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());

    // Protected data:
    protected Map<EntityGroup, ShaderProgram> shaderProgramMap;
    protected KeyEventUtil keyEventUtil;
    protected WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation;

    // Private data:
    private CameraWrapper camera;
    private ImageMetadataPoolLibgdx imageAnalysisPool;
    private Stage stage;
    private float scale = 1.0f;
    private MouseEventUtil mouseEventUtil;
    private EventTriggerController eventTriggerController;
    private ThreadHandler threadHandler;
    private TextureAtlas textureAtlas;
    private SpriteBatch spriteBatch;
    private ShaderProgram ambientShader;
    private ShaderProgram lightShader;
    private EntityPhysicsEngine<EntityLibgdx> entityPhysicsEngine;


    /**
     * @return the scale factor for the pixel art from the native resolution 400 by 225.
     */
    public float getScale() {
        return scale;
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
        stage = new Stage();
        // Setup input handling:
        {
            mouseEventUtil = new MouseEventUtil(this::getWorldRepresentation, this::getCameraX, this::getCameraY, this::getScale);
            keyEventUtil = new KeyEventUtil();
            {
                final var inputProcessor = new InputEventDistributor(mouseEventUtil, keyEventUtil);
                final var imp = new InputMultiplexer();
                imp.setProcessors(inputProcessor, stage);
                Gdx.input.setInputProcessor(imp);
            }
            eventTriggerController = new EventTriggerController(
                    EventTriggerController.InputOptions.registerAll(),
                    keyEventUtil,
                    mouseEventUtil,
                    this::getWorldRepresentation,
                    Functions::getNull,
                    this::getScale
            );
        }
        threadHandler = new ThreadHandler(this::getWorldRepresentation, this::getCameraNeighborhood, this::getDevUI, this::isDeveloperMode);

        textureAtlas = new TextureAtlas("textures.atlas");
        imageAnalysisPool = new ImageMetadataPoolLibgdx(textureAtlas);
        scale = (float) Gdx.graphics.getWidth() / GlobalData.ART_WIDTH;


        logger.setLevel(GlobalData.getLogLevel());
        eventTriggerController.setInputOptions(EventTriggerController.InputOptions.registerAll());
        spriteBatch = new SpriteBatch();

        camera = new CameraWrapper(this::getScale, new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        threadHandler.reload();
        Player.getInstance().ifPresent(p -> p.setHandleInput(true));


        {
            ambientShader = ShaderFactory.ambientShader();
            lightShader = ShaderFactory.lightSourceShader();
            shaderProgramMap = new EnumMap<>(EntityGroup.class);

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
        entityPhysicsEngine = new EntityPhysicsEngine<>();
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
        stage.act(timeSinceLastFrame);


        ambientShader.bind();
        lightShader.bind();

        // Render physics
        if (worldRepresentation != null) {
            final var parameters = new PhysicsParametersImpl(scale, worldRepresentation, keyEventUtil);

            entityPhysicsEngine.renderOutstandingPhysicsFrames(timeSinceLastFrame, parameters, worldRepresentation::forEachEntityInCameraNeighborhood);
        }

        updateCamera(camera);


        spriteBatch.setProjectionMatrix(camera.getCamera().combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        // Render graphics
        if (worldRepresentation != null) {
            {
                final ShaderProgram prg = shaderProgramMap.get(EntityGroup.FOCAL_POINT);
                final LightSourceShaderBinder lssBinder = new LightSourceShaderBinder(prg);
                final ArrayList<LightSourceData> lightSourceData = new ArrayList<>();
                getWorldRepresentation().forEachEntityInCameraNeighborhood(e -> lightSourceData.addAll(e.getLightSourceDataList()));

                final float camXMinusHalfWidth = getCameraX() - getCameraWrapper().getCamera().viewportWidth * 0.5f;
                final float camYMinusHalfHeight = getCameraY() - getCameraWrapper().getCamera().viewportHeight * 0.5f;

                if (!prg.isCompiled()) {
                    logger.warning(prg.getLog());
                    System.exit(1);
                }

                lssBinder.bindData(
                        lightSourceData::get,
                        camXMinusHalfWidth,
                        camYMinusHalfHeight,
                        Math.min(64, lightSourceData.size()),
                        scale
                );
                lssBinder.setAmbient(0.4f, 0.4f, 0.4f, 4f);
            }

            final GraphicsParametersImpl graphicsParameters = new GraphicsParametersImpl(spriteBatch,
                    camera.getCamera(),
                    consumer -> worldRepresentation.actionForEachEntityOrderedByType(consumer),
                    timeSinceLastFrame,
                    scale,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight(),
                    shaderProgramMap);
            worldRepresentation.forEachEntityInCameraNeighborhood(e ->
                    e.updateGraphics(graphicsParameters)
            );
        }

        //bundle.renderFrameByIndex(0);
        //bundle.renderNextAnimationFrame(stateTime);
        //System.out.println(bundle.getCurrentImageAssetPath());

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


}
