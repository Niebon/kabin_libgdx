package dev.kabin.global;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointDouble;
import dev.kabin.utilities.eventhandlers.InputEventDistributor;

import java.util.logging.Level;

public class GlobalData {

    public static final String WORLDS_PATH = "core/assets/worlds/";
    public static final String TEXTURES_PATH = "core/assets/textures.png";
    private static final TextureAtlas atlas = new TextureAtlas("textures.atlas");
    private static final InputProcessor inputProcessor = new InputEventDistributor();
    public static boolean developerMode = true;

    private static final PointDouble scale = Point.of(1.0, 1.0);
    public static Stage stage;
    public static SpriteBatch batch;
    public static float stateTime;
    public static ShapeRenderer shapeRenderer = new ShapeRenderer();
    public static String currentWorld = "world_1.json";

    public static void setScale(double x, double y) {
        scale.setX(x);
        scale.setY(y);
    }

    public static PointDouble getScale() {
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

    public static int artWidth = 400;
    public static int artHeight = 225;

    public static int screenWidth = 400;
    public static int screenHeight = 225;

    public static float scaleFactor = 1.0f;
}
