package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.eventhandlers.*;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import static dev.kabin.GlobalData.*;

public class MainGame extends ApplicationAdapter {

    public static CameraWrapper camera;
    public static int screenWidth = 400;
    public static MutableRectInt currentCameraBounds = MutableRectInt.centeredAt(0, 0, screenWidth, screenHeight);
    public static float scaleFactor = 1.0f;
    private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());
    private final MouseEventUtil mouseEventUtil = GlobalData.mouseEventUtil;
    private SpriteBatch spriteBatch;
    private float stateTime = 0f;

    /**
     * Updates the field {@link #currentCameraBounds} to match the current viewing rectangle the given camera.
     *
     * @param camera the camera to use as reference.
     */
    private void translateCurrentCameraBounds(Camera camera) {
        // Find new camera position:
        currentCameraBounds.translate(
                Math.round(Functions.toIntDivideBy(camera.position.x, scaleFactor) - currentCameraBounds.getCenterX()),
                Math.round(Functions.toIntDivideBy(camera.position.y, scaleFactor) - currentCameraBounds.getCenterY())
        );
    }

    void renderEntityGlobalStateTime(Entity e) {
        e.updateGraphics(new GraphicsParametersImpl(spriteBatch, camera.getCamera(), stateTime, scaleFactor, screenWidth, screenHeight));
    }

    @Override
    public void create() {
        screenWidth = Gdx.graphics.getWidth();
        GlobalData.screenHeight = Gdx.graphics.getHeight();
        scaleFactor = (float) screenWidth / GlobalData.ART_WIDTH;
        mouseEventUtil.setScale(scaleFactor);

        GlobalData.stage = new Stage(new ScreenViewport());
        InputMultiplexer imp = new InputMultiplexer();
        imp.setProcessors(GlobalData.getInputProcessor(), GlobalData.stage);
        Gdx.input.setInputProcessor(imp);
        logger.setLevel(GlobalData.getLogLevel());
        EventUtil.setInputOptions(EventUtil.InputOptions.registerAll(scaleFactor));
        spriteBatch = new SpriteBatch();
        GlobalData.userInterfaceBatch = new SpriteBatch();


        camera = new CameraWrapper(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        DeveloperUI.init(stage);
        GlobalData.atlas = new TextureAtlas("textures.atlas");
        GlobalData.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.

        Player.getInstance().ifPresent(player -> player.setHandleInput(!developerMode));

        // Render physics
        if (GlobalData.getWorldState() != null) {
            final var parameters = new PhysicsParametersImpl(GlobalData.getWorldState(), keyEventUtil);
            PhysicsEngine.render(stateTime, parameters);
        }

        // Admit camera free mode movement if in developer mode.
        if (GlobalData.developerMode) {
            if (!keyEventUtil.isControlDown()) camera.setPos(
                    camera.getCamera().position.x +
                            (keyEventUtil.isPressed(KeyCode.A) == keyEventUtil.isPressed(KeyCode.D) ? 0 :
                                    keyEventUtil.isPressed(KeyCode.A) ? -scaleFactor : scaleFactor),
                    camera.getCamera().position.y +
                            (keyEventUtil.isPressed(KeyCode.S) == keyEventUtil.isPressed(KeyCode.W) ? 0 :
                                    keyEventUtil.isPressed(KeyCode.S) ? -scaleFactor : scaleFactor)
            );
        } else {
            Player.getInstance().ifPresent(camera::follow);
        }


        translateCurrentCameraBounds(camera.getCamera());
        spriteBatch.setProjectionMatrix(camera.getCamera().combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.

        // Render graphics
        if (GlobalData.getWorldState() != null) {
            GlobalData.getWorldState().actionForEachEntityOrderedByType(this::renderEntityGlobalStateTime);
        }

        //bundle.renderFrameByIndex(0);
        //bundle.renderNextAnimationFrame(stateTime);
        //System.out.println(bundle.getCurrentImageAssetPath());

        // Drawing stage last ensures that it occurs before dev.kabin.entities.
        GlobalData.stage.act(stateTime);
        GlobalData.stage.draw();

        //DebugUtil.renderEachCollisionPoint(shapeRenderer, currentCameraBounds, scaleFactor);
        //DebugUtil.renderEachRoot(shapeRenderer, currentCameraBounds, scaleFactor);

        // Render interface:
        if (developerMode) {
            DeveloperUI.updatePositionsOfDraggedEntities();
            DeveloperUI.render(new GraphicsParametersImpl(userInterfaceBatch, camera.getCamera(), stateTime, scaleFactor, screenWidth, screenHeight));
        }
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
    }

    static class GraphicsParametersImpl implements GraphicsParameters {

        @NotNull
        private final SpriteBatch spriteBatch;
        private final float stateTime, scale, screenWidth, screenHeight;
        @NotNull
        private final Camera camera;

        GraphicsParametersImpl(@NotNull SpriteBatch spriteBatch,
                               @NotNull Camera camera,
                               float stateTime,
                               float scale,
                               float screenWidth,
                               float screenHeight) {
            this.spriteBatch = spriteBatch;
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

    }


    public static class CameraWrapper {

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
    }
}
