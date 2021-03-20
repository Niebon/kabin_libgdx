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



    @Override
    public void create() {
        super.create();

        final String devSessionData = Gdx.files.getLocalStoragePath().replace("\\", "/") + "core/assets/dev_session/session.save";
        final JSONObject entityLoadingWidgetSettings;
        final JSONObject tileSelectionWidgetSettings;
        try {
            final JSONObject session = new JSONObject(Files.readString(Path.of(devSessionData)));
            final String pathToWorld = GlobalData.WORLDS_PATH + session.getString("world");
            worldRepresentation = Serializer.loadWorldState(getTextureAtlas(), new JSONObject(Files.readString(Path.of(pathToWorld))));
            entityLoadingWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("entity_selection");
            tileSelectionWidgetSettings = session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("tile_selection");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Initiates the developer interface.
        // In particular adds developer interface listeners to entities.
        DeveloperUI.init(
                stage,
                this::getWorldRepresentation,
                this::getMouseEventUtil,
                this::getKeyEventUtil,
                this::getTextureAtlas,
                this::synchronizer
        );
        DeveloperUI.getEntityLoadingWidget().loadSettings(entityLoadingWidgetSettings);
        DeveloperUI.getTileSelectionWidget().loadSettings(tileSelectionWidgetSettings);
    }

    @Override
    protected void updateCamera(CameraWrapper camera) {
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
    }

    @Override
    public void render() {
        Player.getInstance().ifPresent(player -> player.setHandleInput(!developerMode));

        super.render();

        // Render interface:
        if (developerMode) {
            DeveloperUI.updatePositionsOfDraggedEntities();
            DeveloperUI.render(new GraphicsParametersImpl(userInterfaceBatch,
                    camera.getCamera(),
                    worldRepresentation::forEachEntityInCameraNeighborhood,
                    getStateTime(),
                    scaleFactor,
                    screenWidth,
                    screenHeight));
        }
    }
}
