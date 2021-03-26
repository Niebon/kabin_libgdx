package dev.kabin;

import com.badlogic.gdx.Gdx;
import dev.kabin.entities.impl.Player;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.eventhandlers.KeyCode;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.kabin.GlobalData.*;

public class MainGameDeveloper extends MainGame {

    private DeveloperUI developerUI;

    @Override
    public void create() {
        super.create();

        final String devSessionData = Gdx.files.getLocalStoragePath().replace("\\", "/") + "core/assets/dev_session/session.save";
        final JSONObject entityLoadingWidgetSettings;
        final JSONObject tileSelectionWidgetSettings;
        try {
            final JSONObject session = new JSONObject(Files.readString(Path.of(devSessionData)));
            final String pathToWorld = GlobalData.WORLDS_PATH + session.getString("world");
            worldRepresentation = Serializer.loadWorldState(getTextureAtlas(), new JSONObject(Files.readString(Path.of(pathToWorld))), getScale());
            entityLoadingWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("entity_selection");
            tileSelectionWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("tile_selection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Initiates the developer interface.
        // In particular adds developer interface listeners to entities.
        developerUI = new DeveloperUI();
        developerUI.init(
                stage,
                this::getWorldRepresentation,
                this::getMouseEventUtil,
                this::getKeyEventUtil,
                this::getTextureAtlas,
                this.getCameraWrapper()::getCameraX,
                this.getCameraWrapper()::getCameraY,
                this::getCamBounds,
                this::synchronizer,
                this::getScale
        );
        developerUI.loadEntityLoadingWidgetSettings(entityLoadingWidgetSettings);
        developerUI.loadTileLoadingWidgetSettings(tileSelectionWidgetSettings);
        setDeveloperUISupplier(() -> developerUI);
    }

    @Override
    protected void updateCamera(CameraWrapper camera) {
        // Admit camera free mode movement if in developer mode.
        if (GlobalData.developerMode) {
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
        Player.getInstance().ifPresent(player -> player.setHandleInput(!developerMode));

        super.render();

        // Render interface:
        if (developerMode) {
            developerUI.updatePositionsOfDraggedEntities();
            developerUI.render(new GraphicsParametersImpl(userInterfaceBatch,
                    getCameraWrapper().getCamera(),
                    worldRepresentation::forEachEntityInCameraNeighborhood,
                    getStateTime(),
                    getScale(),
                    screenWidth,
                    screenHeight));
        }
    }
}
