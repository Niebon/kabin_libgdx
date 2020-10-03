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
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.entities.EntityParameters;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;
import dev.kabin.utilities.pools.FontPool;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DevInterface {

    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont BITMAP_FONT_16 = FontPool.find(16);
    private static final EntitySelectionWidget ENTITY_SELECTION_WIDGET = new EntitySelectionWidget();

    public static EntitySelectionWidget getEntitySelectionWidget() {
        return ENTITY_SELECTION_WIDGET;
    }

    /**
     * Initiates the developer interface by activating buttons with respect to the given stage.
     *
     * @param stage a stage.
     */
    public static void init(Stage stage) {
        ENTITY_SELECTION_WIDGET.addToStage(stage);
    }


    public static void addDevCue() {
    }

    public static void saveMap() {
    }

    public static void undoChange() {
    }

    public static void redoChange() {
    }

    public static class EntitySelectionWidget {

        private final Group backingGroup = new Group();
        private String currentlySelectedAsset;
        private EntityFactory.EntityType type = EntityFactory.EntityType.ENTITY_SIMPLE;
        private int layer;

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
                    loadAsset();
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

        public void addEntity() {
            EntityParameters parameters = new EntityParameters.Builder()
                    .setX(MouseEventUtil.getMouseX())
                    .setY(MouseEventUtil.getMouseY())
                    .setLayer(layer)
                    .setScale(GlobalData.scaleFactor)
                    .setAtlasPath(currentlySelectedAsset)
                    .build();

            try {
                Entity e = EntityFactory.EntityType.PLAYER.getMouseClickConstructor().construct(parameters);
                EntityGroupProvider.registerEntity(e);
                e.getActor().ifPresent(GlobalData.stage::addActor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void loadAsset() {
            EXECUTOR_SERVICE.execute(() -> {
                final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                        + "core/assets/raw_textures/";
                JFileChooser chooser = new JFileChooser(relativePath);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                JFrame f = new JFrame();
                f.setVisible(true);
                f.toFront();
                f.setVisible(false);
                int res = chooser.showOpenDialog(f);
                f.dispose();
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    setCurrentlySelectedAsset(selectedFile.getAbsolutePath().replace("\\", "/")
                            .replace(relativePath, ""));
                }
            });
        }


        void showSelectEntityTypeBox() {
            final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            final SelectBox<String> selectBox = new SelectBox<>(skin, "default");
            selectBox.setItems(Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name).toArray(String[]::new));
            selectBox.setSelectedIndex(type.ordinal());
            var dialog = new Dialog("Setting", skin);
            dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
            dialog.getContentTable().defaults().pad(10);
            dialog.getContentTable().add(selectBox);
            dialog.setSize(200, 200);
            dialog.getTitleTable().add(new TextButton("x", skin, "default"))
                    .size(20, 20)
                    .padRight(0).padTop(0);
            dialog.setModal(true);
            backingGroup.addActor(dialog);
            dialog.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    type = EntityFactory.EntityType.valueOf(selectBox.getSelected());
                    backingGroup.removeActor(dialog);
                }
            });
        }

        void addToStage(Stage stage) {
            stage.addActor(backingGroup);
        }

        public void setCurrentlySelectedAsset(String currentlySelectedAsset) {
            this.currentlySelectedAsset = currentlySelectedAsset;
        }

        public void setType(EntityFactory.EntityType type) {
            this.type = type;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }
    }

    public static class TileSelectionWidget {

        public static void addGroundTile() {
        }

        public static void removeGroundTileAtCurrentMousePosition() {
        }
    }

}
