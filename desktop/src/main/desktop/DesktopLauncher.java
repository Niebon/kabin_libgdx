package main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import dev.kabin.MainGame;


public class DesktopLauncher {

    static LwjglApplication application;

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Kabin";
        config.width = 1280;
        config.height = 780;
        config.resizable = true;
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.pot = true;
        settings.fast = true;
        settings.combineSubdirectories = true;
        settings.paddingX = 1;
        settings.paddingY = 1;
        settings.edgePadding = true;
        application = new LwjglApplication(new MainGame(), config);
    }

}
