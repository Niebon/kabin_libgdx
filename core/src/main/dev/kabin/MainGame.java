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
import dev.kabin.entities.Entity;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.Player;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.Functions;
import dev.kabin.util.eventhandlers.EnumWithBoolHandler;
import dev.kabin.util.eventhandlers.EventUtil;
import dev.kabin.util.eventhandlers.KeyCode;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import static dev.kabin.GlobalData.*;

public class MainGame extends ApplicationAdapter {

    public static OrthographicCamera camera;
    public static int screenWidth = 400;
    public static MutableRectInt currentCameraBounds = MutableRectInt.centeredAt(0, 0, screenWidth, screenHeight);
    public static float scaleFactor = 1.0f;
    private SpriteBatch spriteBatch;
    private float stateTime = 0f;
    private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());

    /**
     * Updates the field {@link #currentCameraBounds} to match the current viewing rectangle the given camera.
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
        e.updateGraphics(new GraphicsParametersImpl(spriteBatch, stateTime, camera, scaleFactor, screenWidth, screenHeight));
    }

    @Override
    public void create() {
        screenWidth = Gdx.graphics.getWidth();
        GlobalData.screenHeight = Gdx.graphics.getHeight();
        scaleFactor = (float) screenWidth / GlobalData.ART_WIDTH;


        GlobalData.stage = new Stage(new ScreenViewport());
        InputMultiplexer imp = new InputMultiplexer();
        imp.setProcessors(GlobalData.getInputProcessor(), GlobalData.stage);
        Gdx.input.setInputProcessor(imp);
        logger.setLevel(GlobalData.getLogLevel());
        EventUtil.setInputOptions(EventUtil.InputOptions.getRegisterAll());
        spriteBatch = new SpriteBatch();
        GlobalData.userInterfaceBatch = new SpriteBatch();


        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        DeveloperUI.init(stage);
        GlobalData.atlas = new TextureAtlas("textures.atlas");
        GlobalData.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {

        // Render physics
        if (GlobalData.getWorldState() != null) {
            final var parameters = new PhysicsParametersImpl(GlobalData.getWorldState(), KeyEventUtil.getInstance());
            PhysicsEngine.render(stateTime, parameters);
        }

        // Admit camera free mode movement if in developer mode.
        if (GlobalData.developerMode) {
            var keyEventUtil = KeyEventUtil.getInstance();
            camera.translate(
                    keyEventUtil.isPressed(KeyCode.A) == keyEventUtil.isPressed(KeyCode.D) ? 0 :
                            keyEventUtil.isPressed(KeyCode.A) ? -scaleFactor : scaleFactor,
                    keyEventUtil.isPressed(KeyCode.S) == keyEventUtil.isPressed(KeyCode.W) ? 0 :
                            keyEventUtil.isPressed(KeyCode.S) ? -scaleFactor : scaleFactor
            );
        } else {
            Player.getInstance().ifPresent(player -> camera.translate(player.getX() - camera.position.x, player.getY() - camera.position.y));
        }
        camera.update();


        translateCurrentCameraBounds(camera);
        spriteBatch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
        stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.

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

        // Render interface:
        if (developerMode) {
            DeveloperUI.updatePositionsOfDraggedEntities();
            DeveloperUI.render(GlobalData.userInterfaceBatch, stateTime);
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
            return worldRepresentation.isCollisionAt(x,y);
        }

        @Override
        public boolean isLadderAt(int x, int y) {
            return worldRepresentation.isLadderAt(x,y);
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
    }

    static class GraphicsParametersImpl implements GraphicsParameters{

        @NotNull
        private final SpriteBatch spriteBatch;
        private final float stateTime, scale, screenWidth, screenHeight;
        private final Camera camera;

        GraphicsParametersImpl(@NotNull SpriteBatch spriteBatch,
                               float stateTime,
                               Camera camera,
                               float scale, float screenWidth, float screenHeight) {
            this.spriteBatch = spriteBatch;
            this.stateTime = stateTime;
            this.camera = camera;
            this.scale = scale;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
        }

        @Override
        public SpriteBatch getBatch() {
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

}
