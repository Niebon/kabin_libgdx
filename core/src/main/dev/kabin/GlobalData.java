package dev.kabin;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.eventhandlers.InputEventDistributor;
import dev.kabin.util.points.PointOld;
import dev.kabin.util.points.PointOldDouble;

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
    private static final InputProcessor inputProcessor = new InputEventDistributor();
    private static final PointOldDouble scale = PointOld.of(1.0, 1.0);
    public static TextureAtlas atlas;
    public static boolean developerMode = true;
    public static Stage stage;
    public static SpriteBatch userInterfaceBatch;
    public static ShapeRenderer shapeRenderer;
    public static String currentWorld = "world_1.json";
    public static int screenHeight = 225;
    public static int worldSizeX;
    public static int worldSizeY;


    private static WorldRepresentation worldRepresentation;


    public static void setScale(double x, double y) {
        scale.setX(x);
        scale.setY(y);
    }

    public static PointOldDouble getScale() {
        return scale;
    }

    public static TextureAtlas getAtlas() {
        return atlas;
    }

    public static InputProcessor getInputProcessor() {
        return inputProcessor;
    }

    public static Level getLogLevel() {
        return Level.WARNING;
    }

    public static void setMapSize(int worldSizeX, int worldSizeY) {
        GlobalData.worldSizeX = worldSizeX;
        GlobalData.worldSizeY = worldSizeY;
        worldRepresentation = new WorldRepresentation(worldSizeX, worldSizeY, MainGame.scaleFactor);
    }


    public static WorldRepresentation getWorldState() {
        return worldRepresentation;
    }


    public static void saveDevSession() {
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
                DeveloperUI.getEntityLoadingWidget().toJson(),
                DeveloperUI.getTileSelectionWidget().toJson());
        try {
            Files.writeString(Path.of(DEVELOPER_SESSION_PATH), jsonRepr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
