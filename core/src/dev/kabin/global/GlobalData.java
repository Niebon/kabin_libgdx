package dev.kabin.global;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.utilities.eventhandlers.InputEventDistributor;

public class GlobalData {
    private static final TextureAtlas atlas = new TextureAtlas("textures.atlas");

    private static final InputProcessor inputProcessor = new InputEventDistributor();

    public static TextureAtlas getAtlas() {
        return atlas;
    }

    public static InputProcessor getInputProcessor() {
        return inputProcessor;
    }
}
