package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.entities.EntityFactory;
import dev.kabin.utilities.pools.FontPool;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DevInterface {

    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont BITMAP_FONT_16 = FontPool.find(16);
    private static final EntitySelectionParameters ENTITY_SELECTION_PARAMETERS = new EntitySelectionParameters();
    private static final EntitySelectionWidget ENTITY_SELECTION_WIDGET = new EntitySelectionWidget();

    /**
     * Initiates the developer interface by activating buttons with respect to the given stage.
     *
     * @param stage a stage.
     */
    public static void init(Stage stage) {
        ENTITY_SELECTION_WIDGET.addToStage(stage);
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
                ENTITY_SELECTION_PARAMETERS.currentlySelectedAsset = selectedFile.getAbsolutePath();
            }
        });
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

    static class EntitySelectionParameters {
        String currentlySelectedAsset;
        EntityFactory.EntityType type = EntityFactory.EntityType.ENTITY_SIMPLE;
        int layer;
    }

    static class EntitySelectionWidget {

        private final Group backingGroup = new Group();

        EntitySelectionWidget() {

            final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
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
            backingGroup.addActor(loadImageAssetButton);

            final TextButton setEntityTypeButton = new TextButton("Type", skin, "default");
            setEntityTypeButton.setWidth(150);
            setEntityTypeButton.setHeight(30);
            setEntityTypeButton.setX(0);
            setEntityTypeButton.setY(30);
            setEntityTypeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    showSelectEntityTypeBox();
                    return true;
                }
            });
            backingGroup.addActor(setEntityTypeButton);
        }

        void showSelectEntityTypeBox() {
            final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
            final SelectBox<String> selectBox = new SelectBox<>(skin, "default");
            selectBox.setItems(Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name).toArray(String[]::new));
            selectBox.setSelectedIndex(ENTITY_SELECTION_PARAMETERS.type.ordinal());
            var dialog = new Dialog("Setting", skin);
            dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
            dialog.getContentTable().defaults().pad(10);
            dialog.getContentTable().add(selectBox);
            dialog.setSize(200, 60);
            backingGroup.addActor(dialog);
            dialog.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ENTITY_SELECTION_PARAMETERS.type = EntityFactory.EntityType.valueOf(selectBox.getSelected());
                    backingGroup.removeActor(dialog);
                }
            });
        }

        void addToStage(Stage stage) {
            stage.addActor(backingGroup);
        }

    }

    public static class TileSelectionWidget {

        public static void addGroundTile() {
        }

        public static void removeGroundTileAtCurrentMousePosition() {
        }
    }
}
