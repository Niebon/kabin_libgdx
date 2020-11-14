package dev.kabin;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.global.GlobalData;
import dev.kabin.ui.DeveloperUI;
import dev.kabin.utilities.eventhandlers.EventUtil;

import static dev.kabin.global.GlobalData.*;
import static dev.kabin.utilities.eventhandlers.EnumWithBoolHandler.logger;

public class MainGame extends ApplicationAdapter {

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


        camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        DeveloperUI.init(stage);
    }

    @Override
    public void render() {
        GlobalData.camera.translate(1, 1);
        GlobalData.camera.update();
        GlobalData.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // This cryptic line clears the screen.
        GlobalData.stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time.
        EntityGroupProvider.actionForEachEntityOrderedByGroup(MainGame::renderEntityGlobalStateTime);

        //bundle.renderFrameByIndex(0);
        //bundle.renderNextAnimationFrame(stateTime);
        //System.out.println(bundle.getCurrentImageAssetPath());

        // Drawing stage last ensures that it occurs before dev.kabin.entities.
        GlobalData.stage.act(stateTime);
        GlobalData.stage.draw();

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

}
