package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.entities.EntityFactory;
import dev.kabin.utilities.pools.FontPool;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DevInterface {

    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont font = FontPool.find(16);
    private static final EntitySelectionParameters entitySelectionParameters = new EntitySelectionParameters();

    /**
     * Initiates the developer interface by activating buttons with respect to the given stage.
     *
     * @param stage a stage.
     */
    public static void init(Stage stage) {
        EntitySelectionWidget entitySelectionWidget = new EntitySelectionWidget(stage);
    }

    public static void loadAsset() {
        EXECUTOR_SERVICE.execute(() -> {
            JFileChooser chooser = new JFileChooser(Gdx.files.getLocalStoragePath() + "/core/assets/raw_textures");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                entitySelectionParameters.currentlySelectedAsset = selectedFile.getAbsolutePath();
            }
        });
    }

    public static void addEntity() {

    }

    static class EntitySelectionParameters {
        String currentlySelectedAsset;
        EntityFactory.EntityType type;
        int layer;
    }

    static class EntitySelectionWidget {

        EntitySelectionWidget(Stage stage) {
            Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
            final TextButton loadImageAssetButton = new TextButton("Asset", skin, "default");
            loadImageAssetButton.setWidth(150);
            loadImageAssetButton.setHeight(30);
            loadImageAssetButton.setX(0);
            loadImageAssetButton.setY(0);
            loadImageAssetButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    DevInterface.loadAsset();
                    return true;
                }
            });
            stage.addActor(loadImageAssetButton);

            final TextButton setEntityTypeButton = new TextButton("Type", skin, "default");
            setEntityTypeButton.setWidth(150);
            setEntityTypeButton.setHeight(30);
            setEntityTypeButton.setX(0);
            setEntityTypeButton.setY(30);
            setEntityTypeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    System.out.println("Implement set type window.");
                    return true;
                }
            });
            stage.addActor(setEntityTypeButton);
        }

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
