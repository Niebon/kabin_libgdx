package dev.kabin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.eventhandlers.KeyCode;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class MainGameDeveloper extends MainGame {

    private DeveloperUI developerUI;
    private SpriteBatch developerUISpriteBatch;

    @Override
    public void create() {
        super.create();

        final String devSessionData = Gdx.files.getLocalStoragePath().replace("\\", "/") + "core/assets/dev_session/session.save";
        final JSONObject entityLoadingWidgetSettings;
        final JSONObject tileSelectionWidgetSettings;
        try {
            final JSONObject session = new JSONObject(Files.readString(Path.of(devSessionData)));
            final String pathToWorld = GlobalData.WORLDS_PATH + session.getString("world");
            worldRepresentation = Serializer.loadWorldState(getStage(),
                    getTextureAtlasShaded(),
                    getImageAnalysisPool(),
                    new JSONObject(Files.readString(Path.of(pathToWorld))),
                    getScale());
            entityLoadingWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("entity_selection");
            tileSelectionWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("tile_selection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Initiates the developer interface.
        // In particular adds developer interface listeners to entities.
        developerUI = new DeveloperUI(
                getStage(),
                this::getWorldRepresentation,
                this::getMouseEventUtil,
                this::getKeyEventUtil,
                this::getTextureAtlasShaded,
                this.getCameraWrapper()::getCameraX,
                this.getCameraWrapper()::getCameraY,
                this::getCamBounds,
                this::synchronizer,
                this::getScale,
                this::isDeveloperMode,
                this::getImageAnalysisPool);
        developerUI.loadEntityLoadingWidgetSettings(entityLoadingWidgetSettings);
        developerUI.loadTileLoadingWidgetSettings(tileSelectionWidgetSettings);
        developerUI.setVisible(isDeveloperMode());
        setDeveloperUISupplier(() -> developerUI);
        developerUISpriteBatch = new SpriteBatch();
    }

    @Override
    protected void updateCamera(CameraWrapper camera) {
        // Admit camera free mode movement if in developer mode.
        if (isDeveloperMode()) {
            if (!getKeyEventUtil().isControlDown()) camera.setPos(
                    camera.getCamera().position.x +
                            (getKeyEventUtil().isPressed(KeyCode.A) == getKeyEventUtil().isPressed(KeyCode.D) ? 0 :
                                    getKeyEventUtil().isPressed(KeyCode.A) ? -getScale() : getScale()),
                    camera.getCamera().position.y +
                            (getKeyEventUtil().isPressed(KeyCode.S) == getKeyEventUtil().isPressed(KeyCode.W) ? 0 :
                                    getKeyEventUtil().isPressed(KeyCode.S) ? -getScale() : getScale())
            );
        } else {
            Player.getInstance().ifPresent(camera::follow);
        }
    }

    @Override
    public void render() {
        Player.getInstance().ifPresent(player -> player.setHandleInput(!isDeveloperMode()));

        super.render();

        // Render interface:
        if (isDeveloperMode()) {
            developerUI.updatePositionsOfDraggedEntities();
            developerUISpriteBatch.setShader(null);
            developerUI.render(new GraphicsParametersImpl(developerUISpriteBatch,
                    getCameraWrapper().getCamera(),
                    worldRepresentation::forEachEntityInCameraNeighborhood,
                    Gdx.graphics.getDeltaTime(),
                    getScale(),
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight(),
                    shaderProgramMap));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        developerUISpriteBatch.dispose();
    }

    @Nullable
    protected DeveloperUI getDevUI() {
        return developerUI;
    }

}
