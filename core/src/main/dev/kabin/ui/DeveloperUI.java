package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import dev.kabin.animation.AnimationBundle;
import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.animation.Animations;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityGroupProvider;
import dev.kabin.entities.EntityParameters;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointFloat;
import dev.kabin.geometry.shapes.RectFloat;
import dev.kabin.global.GlobalData;
import dev.kabin.global.WorldStateRecorder;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;
import dev.kabin.utilities.pools.FontPool;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static dev.kabin.global.GlobalData.WORLDS_PATH;

public class DeveloperUI {

    private static final Set<DraggedEntity> CURRENTLY_DRAGGED_ENTITIES = new HashSet<>();
    private static final EntitySelection ENTITY_SELECTION = new EntitySelection();
    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont BITMAP_FONT_16 = FontPool.find(16);
    private static final EntityLoadingWidget ENTITY_LOADING_WIDGET = new EntityLoadingWidget();
    private static final DragListener SELECTION_BEGIN = new DragListener() {
        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            if (CURRENTLY_DRAGGED_ENTITIES.isEmpty() && !getEntityLoadingWidget().dialog.isDragging())
                ENTITY_SELECTION.begin();
        }
    };
    private static final DragListener SELECTION_END = new DragListener() {
        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            ENTITY_SELECTION.end();
        }
    };
    private static JSONObject worldState;

    public static EntitySelection getEntitySelection() {
        return ENTITY_SELECTION;
    }

    public static void init(Stage stage) {
        stage.addListener(new DragListener() {
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                DeveloperUI.clearDraggedEntities();
            }
        });
        DeveloperUI.setVisible(GlobalData.developerMode);
    }

    public static void addEntityToDraggedEntities(Entity e) {
        CURRENTLY_DRAGGED_ENTITIES.add(new DraggedEntity(e.getX(), e.getY(), MouseEventUtil.getMouseX(), MouseEventUtil.getMouseY(), e));
    }

    public static void render(SpriteBatch batch, float stateTime) {
        ENTITY_SELECTION.render();
        ENTITY_LOADING_WIDGET.render(batch, stateTime);
    }

    public static void clearDraggedEntities() {
        CURRENTLY_DRAGGED_ENTITIES.clear();
    }

    public static void updatePositionsOfDraggedEntities() {
        for (DraggedEntity de : CURRENTLY_DRAGGED_ENTITIES) {
            final Entity e = de.getEntity();

            // The update scheme is r -> r + delta mouse. Also, snap to pixels (respecting pixel art).
            e.setX(Functions.snapToPixel(de.getEntityOriginalX() + MouseEventUtil.getMouseX() - de.getInitialMouseX()));
            e.setY(Functions.snapToPixel((de.getEntityOriginalY() + MouseEventUtil.getMouseY() - de.getInitialMouseY())));
        }
    }

    public static void setVisible(boolean b) {
        if (b) {
            GlobalData.stage.addActor(getEntityLoadingWidget().backingGroup);
            GlobalData.stage.addListener(SELECTION_BEGIN);
            GlobalData.stage.addListener(SELECTION_END);
        } else {
            getEntityLoadingWidget().backingGroup.remove();
        }
    }

    public static EntityLoadingWidget getEntityLoadingWidget() {
        return ENTITY_LOADING_WIDGET;
    }

    public static void addDevCue() {
    }

    public static void saveWorld() {
        worldState = WorldStateRecorder.recordWorldState();
        try {
            Files.write(Path.of(WORLDS_PATH + GlobalData.currentWorld), worldState.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void undoChange() {
    }

    public static void redoChange() {
    }

    public static void loadWorld() {
        EXECUTOR_SERVICE.execute(() -> {
            final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                    + "core/assets/worlds/";
            JFileChooser chooser = new JFileChooser(relativePath);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                GlobalData.currentWorld = selectedFile.getName();
                try {
                    WorldStateRecorder.loadWorldState(new JSONObject(Files.readString(selectedFile.toPath())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class EntitySelection {

        private final Set<Entity> currentlySelectedEntities = new HashSet<>();
        private RectFloat backingRect;
        private PointFloat begin;

        void render() {
            if (begin != null) {
                float minX = Math.min(begin.x(), MouseEventUtil.getMouseX());
                float minY = Math.min(begin.y(), MouseEventUtil.getMouseY());
                float width = Math.abs(begin.x() - MouseEventUtil.getMouseX());
                float height = Math.abs(begin.y() - MouseEventUtil.getMouseY());
                backingRect = new RectFloat(minX, minY, width, height);
                GlobalData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GlobalData.shapeRenderer.setColor(0, 1, 1, 1);
                GlobalData.shapeRenderer.rect(begin.x(), begin.y(), MouseEventUtil.getMouseX() - begin.x(),
                        MouseEventUtil.getMouseY() - begin.y());
                GlobalData.shapeRenderer.end();

                // By abuse of the word "render" include this here...
                EntityGroupProvider.actionForEachEntityOrderedByGroup(e -> {
                    if (backingRect.contains(e.getX(), e.getY())) {
                        currentlySelectedEntities.add(e);
                    } else {
                        currentlySelectedEntities.remove(e);
                    }
                });
                System.out.println(currentlySelectedEntities.size());
            }
        }

        void begin() {
            currentlySelectedEntities.clear();
            begin = Point.of(MouseEventUtil.getMouseX(), MouseEventUtil.getMouseY());
        }

        // End, but only clear the selected dev.kabin.entities after the begin() call.
        void end() {
            begin = null;
        }

        public Set<Entity> getCurrentlySelectedEntities() {
            return Collections.unmodifiableSet(currentlySelectedEntities);
        }
    }

    /**
     * Keeps a record of an entity, its current position (x,y), and a mouse position (x,y).
     * Based on this data, we can calculate the new position of the given entity after a mouse drag.
     * The update scheme is:
     * <pre>
     *     entity pos -> entity pos + delta mouse position
     * </pre>
     */
    private static class DraggedEntity {
        private final float entityOriginalX, entityOriginalY;
        private final float initialMouseX, getInitialMouseY;
        private final Entity entity;

        private DraggedEntity(float originalX, float entityOriginalY, float initialMouseX, float getInitialMouseY, Entity entity) {
            this.entityOriginalX = originalX;
            this.entityOriginalY = entityOriginalY;
            this.initialMouseX = initialMouseX;
            this.getInitialMouseY = getInitialMouseY;
            this.entity = entity;
        }

        public float getEntityOriginalX() {
            return entityOriginalX;
        }

        public float getEntityOriginalY() {
            return entityOriginalY;
        }

        public Entity getEntity() {
            return entity;
        }

        public float getInitialMouseX() {
            return initialMouseX;
        }

        public float getInitialMouseY() {
            return getInitialMouseY;
        }
    }

    public static class EntityLoadingWidget {

        private static final int WIDTH = 600;
        private static final int HEIGHT = 200;
        private final Dialog dialog;
        private final Group backingGroup = new Group();
        private AnimationBundle preview = null;
        private String currentlySelectedAsset = "";
        private EntityFactory.EntityType type = EntityFactory.EntityType.ENTITY_SIMPLE;
        private int layer;
        private Label infoMessage;
        private Label contentTableMessage;

        EntityLoadingWidget() {
            var skin = new Skin(Gdx.files.internal("uiskin.json"));

            dialog = new Dialog("Entity loading widget", skin);
            dialog.setBounds(
                    0, 0,
                    WIDTH, HEIGHT
            );


            var loadImageAssetButton = new TextButton("Asset", skin, "default");
            loadImageAssetButton.setWidth(100);
            loadImageAssetButton.setHeight(25);
            loadImageAssetButton.setX(25);
            loadImageAssetButton.setY(25);
            loadImageAssetButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    loadAsset();
                    return true;
                }
            });
            dialog.addActor(loadImageAssetButton);

            var chooseEntityTypeButton = new TextButton("Type", skin, "default");
            chooseEntityTypeButton.setWidth(100);
            chooseEntityTypeButton.setHeight(25);
            chooseEntityTypeButton.setX(25);
            chooseEntityTypeButton.setY(50);
            chooseEntityTypeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    showSelectEntityTypeBox();
                    return true;
                }
            });
            dialog.addActor(chooseEntityTypeButton);
            backingGroup.addActor(dialog);

            refreshContentTableMessage();
        }

        void refreshContentTableMessage() {
            final int maxLength = 10;
            dialog.getContentTable().clear();
            dialog.getContentTable().defaults();
            dialog.removeActor(contentTableMessage);
            contentTableMessage = new Label("asset:  " +
                    (currentlySelectedAsset.length() < maxLength ? currentlySelectedAsset
                            : (currentlySelectedAsset.substring(0, maxLength) + "...")) +
                    '\n' +
                    "type:  " +
                    type.name() +
                    '\n' +
                    "layer: " +
                    layer, dialog.getSkin());
            contentTableMessage.setAlignment(Align.left);
            contentTableMessage.setPosition(0, 100);
            dialog.addActor(contentTableMessage);
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
                Entity e = type.getMouseClickConstructor().construct(parameters);
                EntityGroupProvider.registerEntity(e);
                float offsetX = e.getPixelMassCenterX() * e.getScale();
                float offsetY = e.getPixelMassCenterY() * e.getScale();
                e.setPos(e.getX() - offsetX, e.getY() - offsetY);
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
                    preview = AnimationBundleFactory.loadFromAtlasPath(currentlySelectedAsset);
                }
                refreshContentTableMessage();
            });
        }


        void showSelectEntityTypeBox() {
            var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            var selectBox = new SelectBox<String>(skin, "default");
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
                    refreshContentTableMessage();
                }
            });
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

        public void render(SpriteBatch batch, float stateTime) {
            if (preview != null) {
                preview.setCurrentAnimation(Animations.AnimationType.DEFAULT_RIGHT);
                float scale = 4.0f * preview.getOriginalWidth() / 32;
                preview.setScale(scale);
                preview.setPos(0.75f * WIDTH + dialog.getX(), dialog.getY());
                preview.renderNextAnimationFrame(batch, stateTime);
            }
        }
    }

    public static class TileSelectionWidget {

        public static void addGroundTile() {
        }

        public static void removeGroundTileAtCurrentMousePosition() {
        }
    }

}
