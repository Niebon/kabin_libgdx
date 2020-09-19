package dev.kabin.global;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointDouble;
import dev.kabin.utilities.eventhandlers.InputEventDistributor;

public class GlobalData {

	private static final TextureAtlas atlas = new TextureAtlas("textures.atlas");
	private static final InputProcessor inputProcessor = new InputEventDistributor();
	private static PointDouble scale = Point.of(1.0, 1.0);

	public static PointDouble getScale() {
		return scale;
	}

	public static TextureAtlas getAtlas() {
		return atlas;
	}

	public static InputProcessor getInputProcessor() {
		return inputProcessor;
	}
}
