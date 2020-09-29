package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.kabin.utilities.pools.FontPool;

import javax.swing.*;
import java.io.File;

public class DevInterface {

    private static final BitmapFont font = FontPool.find(16);

    private static String currentlySelectedAsset;

    public static void loadAsset() {
        new Thread(() -> {
            JFileChooser chooser = new JFileChooser(Gdx.files.getLocalStoragePath());
            JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                currentlySelectedAsset = selectedFile.getAbsolutePath();
            }
        }).start();
    }

    public static void addEntity() {
    }

    public static void addDevCue() {
    }

    public static void saveMap() {
    }

    public static void undoChange() {
    }

    public static void redoChange() {
    }

    public static class TileSelectionWidget {

        public static void addGroundTile() {
        }

        public static void removeGroundTileAtCurrentMousePosition() {
        }
    }
}
