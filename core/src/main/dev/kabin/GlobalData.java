package dev.kabin;

import dev.kabin.ui.developer.DeveloperUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class GlobalData {

    public static final String DEVELOPER_SESSION_PATH = "core/assets/dev_session/session.save";
    public static final String WORLDS_PATH = "core/assets/worlds/";
    public static final String TEXTURES_PATH = "core/assets/textures.png";
    public static final int ART_WIDTH = 400;
    public static final int ART_HEIGHT = 225;
    public static String currentWorld = "world_1.json";
    public static int worldSizeX;
    public static int worldSizeY;


    public static Level getLogLevel() {
        return Level.WARNING;
    }

    public static void saveDevSession(DeveloperUI developerUI, boolean developerMode) {
        final String jsonRepr = """
                {
                  "world" : "%s",
                  "developer" : {
                    "dev_mode" : %s,
                    "widgets" : {
                      "entity_selection" : %s,
                      "tile_selection" : %s
                    }
                  }
                }
                """.formatted(currentWorld,
                developerMode,
                developerUI.entityLoadingWidgetToJson(),
                developerUI.tileLoadingWidgetToJson());
        try {
            Files.writeString(Path.of(DEVELOPER_SESSION_PATH), jsonRepr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
