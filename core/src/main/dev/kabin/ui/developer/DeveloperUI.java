package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.Serializer;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.impl.Entity;
import dev.kabin.ui.developer.widgets.DraggedEntity;
import dev.kabin.ui.developer.widgets.EntityLoadingWidget;
import dev.kabin.ui.developer.widgets.TileSelectionWidget;
import dev.kabin.util.Functions;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.functioninterfaces.FloatSupplier;
import dev.kabin.util.shapes.primitive.RectInt;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.kabin.GlobalData.WORLDS_PATH;
import static dev.kabin.GlobalData.atlas;

public class DeveloperUI {

    public static final String OPEN = "open";
    public static final String SAVE = "save";
    public static final String SAVE_AS = "save as";
    private final Set<DraggedEntity> CURRENTLY_DRAGGED_ENTITIES = new HashSet<>();
    private final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private final SelectBox<Button> FILE_DROP_DOWN_MENU = new SelectBox<>(new Skin(Gdx.files.internal("default/skin/uiskin.json")), "default");
    private Supplier<WorldRepresentation> worldRepresentationSupplier = Functions.nullSupplier();
    private Supplier<MouseEventUtil> mouseEventUtilSupplier = Functions.nullSupplier();
    private EntitySelection entitySelection;
    private final DragListener SELECTION_END = new DragListener() {
        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            getEntitySelection().end();
        }
    };
    private Supplier<KeyEventUtil> keyEventUtilSupplier = Functions.nullSupplier();
    private EntityLoadingWidget entityLoadingWidget;
    private TileSelectionWidget tileSelectionWidget;
    private final DragListener selectionBegin = new DragListener() {
        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            if (
                    !keyEventUtilSupplier.get().isAltDown() &&
                            CURRENTLY_DRAGGED_ENTITIES.isEmpty() &&
                            !entityLoadingWidget.getWidget().isDragging() &&
                            !tileSelectionWidget.getWidget().isDragging()
            ) {
                getEntitySelection().begin();
            }
        }
    };
    private FloatSupplier scale;

    public EntitySelection getEntitySelection() {
        return entitySelection;
    }

    public void init(Stage stage,
                     Supplier<WorldRepresentation> worldRepresentationSupplier,
                     Supplier<MouseEventUtil> mouseEventUtilSupplier,
                     Supplier<KeyEventUtil> keyEventUtilSupplier,
                     Supplier<TextureAtlas> textureAtlasSupplier,
                     FloatSupplier camPosX,
                     FloatSupplier camPosY,
                     Supplier<RectInt> camBounds,
                     Consumer<Runnable> synchronizer,
                     FloatSupplier scale) {
        this.scale = scale;

        entitySelection = new EntitySelection(mouseEventUtilSupplier, camPosX, camPosY);

        entityLoadingWidget = new EntityLoadingWidget(
                EXECUTOR_SERVICE,
                () -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                () -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                scale,
                e -> worldRepresentationSupplier.get().registerEntity(e),
                textureAtlasSupplier);
        tileSelectionWidget = new TileSelectionWidget(
                textureAtlasSupplier,
                EXECUTOR_SERVICE,
                () -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                () -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                scale,
                camBounds,
                worldRepresentationSupplier,
                synchronizer
        );


        stage.addListener(new DragListener() {
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                DeveloperUI.this.clearDraggedEntities();
            }
        });
        this.setVisible(GlobalData.developerMode);
        this.worldRepresentationSupplier = worldRepresentationSupplier;
        this.mouseEventUtilSupplier = mouseEventUtilSupplier;
        this.keyEventUtilSupplier = keyEventUtilSupplier;


        // Add drop down menu:
        var buttonOpen = new Button();
        buttonOpen.setName(OPEN);
        var buttonSave = new Button();
        buttonSave.setName(SAVE);
        var buttonSaveAs = new Button();
        buttonSaveAs.setName(SAVE_AS);
        FILE_DROP_DOWN_MENU.setItems(buttonOpen, buttonSave, buttonSaveAs);
        FILE_DROP_DOWN_MENU.setSelectedIndex(0);
        FILE_DROP_DOWN_MENU.setPosition(0f, MainGame.screenHeight - FILE_DROP_DOWN_MENU.getHeight());
        FILE_DROP_DOWN_MENU.setName("File");
        FILE_DROP_DOWN_MENU.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                switch (FILE_DROP_DOWN_MENU.getSelected().getName()) {
                    case OPEN:
                        loadWorld();
                        break;
                    case SAVE:
                        saveWorld();
                        break;
                    case SAVE_AS:
                        saveWorldAs();
                        break;
                    default:
                        break;
                }
            }
        });
        stage.addActor(FILE_DROP_DOWN_MENU);
        worldRepresentationSupplier.get().actionForEachEntityOrderedByType(e ->
                e.getActor().ifPresent(a -> a.addListener(new ClickListener() {
                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                if (button != Input.Buttons.RIGHT) return false;
                                if (GlobalData.developerMode) {
                                    final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
                                    final var dialog = new Dialog("Actions", skin);
                                    final float width = 200;
                                    final float height = 200;
                                    dialog.setBounds(
                                            mouseEventUtilSupplier.get().getXRelativeToUI() + width * 0.1f,
                                            mouseEventUtilSupplier.get().getYRelativeToUI() + height * 0.1f,
                                            width, height
                                    );
                                    dialog.getContentTable().defaults().pad(10);

                                    // Remove button.
                                    final var removeButton = new TextButton("Remove", skin, "default");
                                    removeButton.addListener(
                                            new ClickListener() {
                                                @Override
                                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                                                    // Find all dev.kabin.entities scheduled for removal.
                                                    final Set<Entity> entitiesScheduledForRemoval = new HashSet<>();
                                                    final Set<Entity> currentlySelectedEntities = getEntitySelection()
                                                            .getCurrentlySelectedEntities();
                                                    if (currentlySelectedEntities.contains(e)) {
                                                        entitiesScheduledForRemoval.addAll(currentlySelectedEntities);
                                                    } else {
                                                        entitiesScheduledForRemoval.add(e);
                                                    }

                                                    entitiesScheduledForRemoval.forEach(e -> {
                                                        worldRepresentationSupplier.get().unregisterEntity(e);
                                                        e.getActor().ifPresent(Actor::remove);
                                                    });

                                                    dialog.remove();
                                                    return true;
                                                }
                                            }
                                    );
                                    dialog.getContentTable().add(removeButton).size(100, 30);

                                    // Exit button.
                                    var exitButton = new TextButton("x", skin, "default");
                                    exitButton.addListener(
                                            new ClickListener() {
                                                @Override
                                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                                    return dialog.remove();
                                                }
                                            }
                                    );
                                    dialog.getTitleTable().add(exitButton)
                                            .size(20, 20)
                                            .padRight(0).padTop(0);
                                    dialog.setModal(true);
                                    GlobalData.stage.addActor(dialog);
                                }
                                return true;
                            }
                        })
                )
        );


        worldRepresentationSupplier.get().actionForEachEntityOrderedByType(this::addDragListenerToEntity);
    }

    private void saveWorldAs() {
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

    public void addEntityToDraggedEntities(Entity e) {
        CURRENTLY_DRAGGED_ENTITIES.add(new DraggedEntity(
                e.getX(),
                e.getY(),
                mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                e));
    }

    public void render(GraphicsParameters params) {
        entitySelection.render(params.forEachEntityInCameraNeighborhood());
        entityLoadingWidget.render(params);
        tileSelectionWidget.render(params);
    }

    public void clearDraggedEntities() {
        CURRENTLY_DRAGGED_ENTITIES.clear();
    }

    public void updatePositionsOfDraggedEntities() {
        for (DraggedEntity de : CURRENTLY_DRAGGED_ENTITIES) {
            final Entity e = de.getEntity();

            // The update scheme is r -> r + delta mouse. Also, snap to pixels (respecting pixel art).
            e.setX(Functions.snapToPixel(de.getEntityOriginalX() + mouseEventUtilSupplier.get().getMouseXRelativeToWorld() - de.getInitialMouseX(), scale.get()));
            e.setY(Functions.snapToPixel(de.getEntityOriginalY() + mouseEventUtilSupplier.get().getMouseYRelativeToWorld() - de.getInitialMouseY(), scale.get()));
        }
    }

    public void setVisible(boolean b) {
        entityLoadingWidget.getWidget().setVisible(b);
        tileSelectionWidget.getWidget().setVisible(b);
        if (b) {
            GlobalData.stage.addListener(selectionBegin);
            GlobalData.stage.addListener(SELECTION_END);
        } else {
            GlobalData.stage.removeListener(selectionBegin);
            GlobalData.stage.removeListener(SELECTION_END);
        }
    }

    public void addDevCue() {
    }

    public void saveWorld() {
        saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
    }

    public void saveWorld(Path path) {
        JSONObject worldState = Serializer.recordWorldState(worldRepresentationSupplier.get(), scale.get());
        try {
            Files.write(path, worldState.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void undoChange() {
    }

    public void redoChange() {
    }

    public void loadWorld() {
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

                    // TODO: deal with.
                    Serializer.loadWorldState(atlas, new JSONObject(Files.readString(selectedFile.toPath())), scale.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JSONObject entityLoadingWidgetToJson() {
        return entityLoadingWidget.toJson();
    }

    public JSONObject tileLoadingWidgetToJson() {
        return tileSelectionWidget.toJson();
    }

    public void loadEntityLoadingWidgetSettings(JSONObject settings) {
        entityLoadingWidget.loadSettings(settings);
    }

    public void loadTileLoadingWidgetSettings(JSONObject settings) {
        tileSelectionWidget.loadSettings(settings);
    }

    public void addEntity() {
        entityLoadingWidget.addEntity().ifPresent(this::addDragListenerToEntity);
    }



    private void addDragListenerToEntity(Entity e) {
        e.getActor().ifPresent(
                actor -> actor.addListener(new DragListener() {
                    @Override
                    public void dragStart(InputEvent event, float x, float y, int pointer) {
                        if (DeveloperUI.this.getEntitySelection().getCurrentlySelectedEntities().isEmpty()) {
                            DeveloperUI.this.addEntityToDraggedEntities(e);
                        } else {
                            DeveloperUI.this.getEntitySelection().getCurrentlySelectedEntities()
                                    .forEach(DeveloperUI.this::addEntityToDraggedEntities);
                        }
                    }
                }));
    }

    public void replaceCollisionTileAtCurrentMousePositionWithCurrentSelection() {
        tileSelectionWidget.replaceCollisionTileAtCurrentMousePositionWithCurrentSelection();
    }

    public void removeGroundTileAtCurrentMousePositionThreadLocked() {
        tileSelectionWidget.removeGroundTileAtCurrentMousePositionThreadLocked();
    }
}
