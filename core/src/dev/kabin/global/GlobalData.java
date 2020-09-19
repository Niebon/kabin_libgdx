package dev.kabin.global;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointDouble;
import dev.kabin.utilities.eventhandlers.InputEventDistributor;

import java.util.logging.Level;

public class GlobalData {

	private static final TextureAtlas atlas = new TextureAtlas("textures.atlas");
	private static final InputProcessor inputProcessor = new InputEventDistributor();

	private static final PointDouble scale = Point.of(1.0, 1.0);

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
}
