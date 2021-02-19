package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import dev.kabin.GlobalData;
import dev.kabin.WorldStateRecorder;
import dev.kabin.entities.*;
import dev.kabin.ui.developer.widgets.DraggedEntity;
import dev.kabin.ui.developer.widgets.EntityLoadingWidget;
import dev.kabin.ui.developer.widgets.TileSelectionWidget;
import dev.kabin.util.Functions;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.pools.FontPool;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static dev.kabin.GlobalData.WORLDS_PATH;

public class DeveloperUI {

    public static final String OPEN = "open";
    public static final String SAVE = "save";
    public static final String SAVE_AS = "save as";
    private static final Set<DraggedEntity> CURRENTLY_DRAGGED_ENTITIES = new HashSet<>();
    private static final EntitySelection ENTITY_SELECTION = new EntitySelection();
    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont BITMAP_FONT_16 = FontPool.find(16);
    private static final EntityLoadingWidget ENTITY_LOADING_WIDGET = new EntityLoadingWidget(EXECUTOR_SERVICE);
    private static final TileSelectionWidget TILE_SELECTION_WIDGET = new TileSelectionWidget(EXECUTOR_SERVICE);
    private static final SelectBox<Button> FILE_DROP_DOWN_MENU = new SelectBox<>(new Skin(Gdx.files.internal("default/skin/uiskin.json")), "default");
    private static final DragListener SELECTION_BEGIN = new DragListener() {
        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            if (
                    !KeyEventUtil.isAltDown() &&
                            CURRENTLY_DRAGGED_ENTITIES.isEmpty() &&
                            !ENTITY_LOADING_WIDGET.getWidget().isDragging() &&
                            !TILE_SELECTION_WIDGET.getWidget().isDragging()
            ) {
                ENTITY_SELECTION.begin();
            }
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


        // Add drop down menu:
        var buttonOpen = new Button();
        buttonOpen.setName(OPEN);
        var buttonSave = new Button();
        buttonSave.setName(SAVE);
        var buttonSaveAs = new Button();
        buttonSaveAs.setName(SAVE_AS);
        FILE_DROP_DOWN_MENU.setItems(buttonOpen, buttonSave, buttonSaveAs);
        FILE_DROP_DOWN_MENU.setSelectedIndex(0);
        FILE_DROP_DOWN_MENU.setPosition(0f, GlobalData.screenHeight - FILE_DROP_DOWN_MENU.getHeight());
        FILE_DROP_DOWN_MENU.setName("File");
        FILE_DROP_DOWN_MENU.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                switch (FILE_DROP_DOWN_MENU.getSelected().getName()) {
                    case OPEN:
                        DeveloperUI.loadWorld();
                        break;
                    case SAVE:
                        DeveloperUI.saveWorld();
                        break;
                    case SAVE_AS:
                        DeveloperUI.saveWorldAs();
                        break;
                    default:
                        break;
                }
            }
        });
        stage.addActor(FILE_DROP_DOWN_MENU);
    }

    private static void saveWorldAs() {
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
                saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
            }
        });
    }

    public static void addEntityToDraggedEntities(Entity e) {
        CURRENTLY_DRAGGED_ENTITIES.add(new DraggedEntity(e.getX(),
                e.getY(),
                MouseEventUtil.getMouseXRelativeToWorld(),
                MouseEventUtil.getMouseYRelativeToWorld(),
                e));
    }

    public static void render(SpriteBatch batch, float stateTime) {
        ENTITY_SELECTION.render();
        ENTITY_LOADING_WIDGET.render(batch, stateTime);
        TILE_SELECTION_WIDGET.render(batch);
    }

    public static void clearDraggedEntities() {
        CURRENTLY_DRAGGED_ENTITIES.clear();
    }

    public static void updatePositionsOfDraggedEntities() {
        for (DraggedEntity de : CURRENTLY_DRAGGED_ENTITIES) {
            final Entity e = de.getEntity();

            // The update scheme is r -> r + delta mouse. Also, snap to pixels (respecting pixel art).
            e.setX(Functions.snapToPixel(de.getEntityOriginalX() + MouseEventUtil.getMouseXRelativeToWorld() - de.getInitialMouseX()));
            e.setY(Functions.snapToPixel(de.getEntityOriginalY() + MouseEventUtil.getMouseYRelativeToWorld() - de.getInitialMouseY()));
        }
    }

    public static void setVisible(boolean b) {
        ENTITY_LOADING_WIDGET.getWidget().setVisible(b);
        TILE_SELECTION_WIDGET.getWidget().setVisible(b);
        if (b) {
            GlobalData.stage.addListener(SELECTION_BEGIN);
            GlobalData.stage.addListener(SELECTION_END);
        } else {
            GlobalData.stage.removeListener(SELECTION_BEGIN);
            GlobalData.stage.removeListener(SELECTION_END);
        }
    }

    public static void addDevCue() {
    }

    public static void saveWorld() {
        saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
    }

    public static void saveWorld(Path path) {
        JSONObject worldState = WorldStateRecorder.recordWorldState();
        try {
            Files.write(path, worldState.toString().getBytes());
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

    public static EntityLoadingWidget getEntityLoadingWidget() {
        return ENTITY_LOADING_WIDGET;
    }

    public static TileSelectionWidget getTileSelectionWidget() {
        return TILE_SELECTION_WIDGET;
    }

}
