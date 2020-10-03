package dev.kabin.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import dev.kabin.MainGame;

public class DesktopLauncher {

    static LwjglApplication application;


    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title = "Kabin";
        config.width = 1920;
        config.height = 1080;
        config.resizable = true;


        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.pot = true;
        settings.fast = true;
        settings.combineSubdirectories = true;
        settings.paddingX = 1;
        settings.paddingY = 1;
        settings.edgePadding = true;
        TexturePacker.process(settings, "core/assets/raw_textures", "./core/assets/", "textures");

        application = new LwjglApplication(new MainGame(), config);
    }
}
