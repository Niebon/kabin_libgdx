package dev.kabin;

import com.badlogic.gdx.Gdx;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.ui.developer.widgets.TileSelectionWidget;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainGameDeveloper extends MainGame {



    @Override
    public void create() {
        super.create();


        final String root = Gdx.files.getLocalStoragePath().replace("\\", "/") + "core/assets/";
        final String sessionData = root + "saves/session.save";

        try{
            final JSONObject session = new JSONObject(Files.readString(Path.of(sessionData)));
            final String pathToWorld = GlobalData.WORLDS_PATH + session.getString("world");
            WorldStateRecorder.loadWorldState(new JSONObject(Files.readString(Path.of(pathToWorld))));
            DeveloperUI.getEntityLoadingWidget().loadSettings(session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("entity_selection"));
            DeveloperUI.getTileSelectionWidget().loadSettings(session.getJSONObject("developer").getJSONObject("widgets").getJSONObject("tile_selection"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
