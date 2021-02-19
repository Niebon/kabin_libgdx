package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.Entity;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.Player;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.eventhandlers.EnumWithBoolHandler;
import dev.kabin.util.eventhandlers.EventUtil;
import dev.kabin.util.eventhandlers.KeyCode;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

import static dev.kabin.GlobalData.*;

public class MainGame extends ApplicationAdapter {

    private final Logger logger = Logger.getLogger(EnumWithBoolHandler.class.getName());

    static void renderEntityGlobalStateTime(Entity e) {
        e.render(GlobalData.batch, GlobalData.stateTime);
    }

    @Override
    public void create() {
        GlobalData.screenWidth = Gdx.graphics.getWidth();
        GlobalData.screenHeight = Gdx.graphics.getHeight();
        GlobalData.scaleFactor = (float) GlobalData.screenWidth / GlobalData.artWidth;


        GlobalData.stage = new Stage(new ScreenViewport());
        InputMultiplexer imp = new InputMultiplexer();
        imp.setProcessors(GlobalData.getInputProcessor(), GlobalData.stage);
        Gdx.input.setInputProcessor(imp);
        logger.setLevel(GlobalData.getLogLevel());
        EventUtil.setInputOptions(EventUtil.InputOptions.getRegisterAll());
        GlobalData.batch = new SpriteBatch();
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


        GlobalData.updateCameraLocation();
        GlobalData.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
        GlobalData.stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.

        // Render graphics
        if (GlobalData.getWorldState() != null) {
            GlobalData.getWorldState().actionForEachEntityOrderedByType(MainGame::renderEntityGlobalStateTime);
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
            DeveloperUI.render(GlobalData.userInterfaceBatch, GlobalData.stateTime);
        }
    }

    @Override
    public void dispose() {
        GlobalData.userInterfaceBatch.dispose();
        GlobalData.batch.dispose();
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

}
