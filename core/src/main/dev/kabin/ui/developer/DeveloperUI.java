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
import dev.kabin.Serializer;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.util.Functions;
import dev.kabin.util.eventhandlers.KeyEventUtil;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.lambdas.BooleanSupplier;
import dev.kabin.util.lambdas.FloatSupplier;
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

public class DeveloperUI {

    // Constants
    public static final String OPEN = "open";
    public static final String SAVE = "save";
    public static final String SAVE_AS = "save as";

    // Class fields
    private final Set<DraggedEntity> draggedEntities = new HashSet<>();
    private final Executor executorService = Executors.newSingleThreadExecutor();
    private final SelectBox<Button> fileDropDownMenu = new SelectBox<>(new Skin(Gdx.files.internal("default/skin/uiskin.json")), "default");
    private final Supplier<WorldRepresentation<EntityGroup, EntityLibgdx>> worldRepresentationSupplier;
    private final Supplier<MouseEventUtil> mouseEventUtilSupplier;
    private final Supplier<TextureAtlas> textureAtlasSupplier;
    private final BooleanSupplier developerMode;
    private final Supplier<ImageMetadataPoolLibgdx> imageAnalysisPoolSupplier;
    private final FloatSupplier scale;
    private final EntitySelection entitySelection;
    private final DragListener selectionEnd = new DragListener() {
        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            getEntitySelection().end();
        }
    };
    private final Supplier<KeyEventUtil> keyEventUtilSupplier;
    private final EntityLoadingWidget entityLoadingWidget;
    private final TileSelectionWidget tileSelectionWidget;
    private final DragListener selectionBegin = new DragListener() {
        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            if (
                    !keyEventUtilSupplier.get().isAltDown() &&
                            draggedEntities.isEmpty()
//                            !entityLoadingWidget.getWidget().isDragging() &&
//                            !tileSelectionWidget.getWidget().isDragging()
            ) {
                getEntitySelection().begin();
            }
        }
    };
    private final Stage stage;


    public DeveloperUI(Stage stage,
                       Supplier<WorldRepresentation<EntityGroup, EntityLibgdx>> worldRepresentationSupplier,
                       Supplier<MouseEventUtil> mouseEventUtilSupplier,
                       Supplier<KeyEventUtil> keyEventUtilSupplier,
                       Supplier<TextureAtlas> textureAtlasSupplier,
                       FloatSupplier camPosX,
                       FloatSupplier camPosY,
                       Supplier<RectInt> camBounds,
                       Consumer<Runnable> synchronizer,
                       FloatSupplier scale,
                       BooleanSupplier developerMode,
                       Supplier<ImageMetadataPoolLibgdx> imageAnalysisPoolSupplier) {
        this.textureAtlasSupplier = textureAtlasSupplier;

        this.scale = scale;
        this.stage = stage;
        this.developerMode = developerMode;
        this.imageAnalysisPoolSupplier = imageAnalysisPoolSupplier;



        entityLoadingWidget = new EntityLoadingWidget(
                stage,
                executorService,
                () -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                () -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                scale,
                e -> worldRepresentationSupplier.get().registerEntity(e),
                textureAtlasSupplier,
                imageAnalysisPoolSupplier);
        tileSelectionWidget = new TileSelectionWidget(
                stage,
                textureAtlasSupplier,
                executorService,
                () -> mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                () -> mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                scale,
                camBounds,
                worldRepresentationSupplier,
                imageAnalysisPoolSupplier,
                synchronizer
        );
        entitySelection = new EntitySelection(mouseEventUtilSupplier, camPosX, camPosY);

        entitySelection.receiveDragListenerFrom(entityLoadingWidget.getWidget().getWindow());
        entitySelection.receiveDragListenerFrom(tileSelectionWidget.getWidget().getWindow());


        stage.addListener(new DragListener() {
            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                DeveloperUI.this.clearDraggedEntities();
            }
        });
        this.setVisible(developerMode.isTrue());
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
        fileDropDownMenu.setItems(buttonOpen, buttonSave, buttonSaveAs);
        fileDropDownMenu.setSelectedIndex(0);
        fileDropDownMenu.setPosition(0f, Gdx.graphics.getHeight() - fileDropDownMenu.getHeight());
        fileDropDownMenu.setName("File");
        fileDropDownMenu.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                switch (fileDropDownMenu.getSelected().getName()) {
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
        stage.addActor(fileDropDownMenu);
        worldRepresentationSupplier.get().actionForEachEntityOrderedByType(this::initializeModificationDialogBoxFor);
        worldRepresentationSupplier.get().actionForEachEntityOrderedByType(this::addDragListenerToEntity);
    }

    public EntitySelection getEntitySelection() {
        return entitySelection;
    }

    private void initializeModificationDialogBoxFor(EntityLibgdx e) {
        e.getActor().ifPresent(a -> a.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button != Input.Buttons.RIGHT) {
                    return false;
                }

                if (developerMode.isFalse()) {
                    return false;
                }

                final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
                final var window = new Window("Actions", skin);
                entitySelection.receiveDragListenerFrom(window);
                final float width = 200;
                final float height = 200;
                window.setBounds(
                        mouseEventUtilSupplier.get().getXRelativeToUI() + width * 0.1f,
                        mouseEventUtilSupplier.get().getYRelativeToUI() + height * 0.1f,
                        width, height
                );

                // Remove button.
                final var removeButton = new TextButton("Remove", skin, "default");
                removeButton.setY(70);
                removeButton.setX(50);
                removeButton.setWidth(100);
                removeButton.setHeight(25);
                removeButton.addListener(
                        new ClickListener() {
                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                                // Find all dev.kabin.entities scheduled for removal.
                                final Set<EntityLibgdx> entitiesScheduledForRemoval = new HashSet<>();
                                final Set<EntityLibgdx> currentlySelectedEntities = getEntitySelection()
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

                                window.remove();
                                return true;
                            }
                        }
                );
                window.addActor(removeButton);


                final var modifyShaderButton = new TextButton("Shaders", skin, "default");
                modifyShaderButton.setY(35);
                modifyShaderButton.setX(50);
                modifyShaderButton.setWidth(100);
                modifyShaderButton.setHeight(25);
                modifyShaderButton.addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        showShaderModificationWindowFor(e);
                        return super.touchDown(event, x, y, pointer, button);
                    }
                });
                window.addActor(modifyShaderButton);


                // Exit button.
                final var exitButton = new TextButton("x", skin, "default");
                exitButton.addListener(
                        new ClickListener() {
                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                window.remove();
                                entitySelection.removeDragListenerTo(window);
                                return true;
                            }
                        }
                );
                window.getTitleTable().add(exitButton)
                        .size(20, 20)
                        .padRight(0).padTop(0);
                window.setModal(true);
                stage.addActor(window);

                return true;
            }
        }));
    }

    private void showShaderModificationWindowFor(EntityLibgdx e) {
        new ModifyShaderWindow(stage, entitySelection, mouseEventUtilSupplier.get(), e);

    }

    private void saveWorldAs() {
        executorService.execute(() -> {
            final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                    + "core/assets/worlds/";
            final JFileChooser chooser = new JFileChooser(relativePath);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            final JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            final int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                GlobalData.currentWorld = selectedFile.getName();
                saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
            }
        });
    }

    public void addEntityToDraggedEntities(EntityLibgdx e) {
        draggedEntities.add(new DraggedEntity(
                e.getX() * scale.get(),
                e.getY() * scale.get(),
                mouseEventUtilSupplier.get().getMouseXRelativeToWorld(),
                mouseEventUtilSupplier.get().getMouseYRelativeToWorld(),
                e));
    }

    public void render(GraphicsParametersLibgdx params) {
        entitySelection.render(params.forEachEntityInCameraNeighborhood());
        entityLoadingWidget.render(params);
        tileSelectionWidget.render(params);
    }

    public void clearDraggedEntities() {
        draggedEntities.clear();
    }

    public void updatePositionsOfDraggedEntities() {
        for (DraggedEntity de : draggedEntities) {
            final EntityLibgdx e = de.getEntity();

            // The update scheme is r -> r + delta mouse. Also, snap to pixels (respecting pixel art).
            final float targetX = de.getEntityOriginalX() + mouseEventUtilSupplier.get().getMouseXRelativeToWorld() - de.getInitialMouseX();
            final float targetY = de.getEntityOriginalY() + mouseEventUtilSupplier.get().getMouseYRelativeToWorld() - de.getInitialMouseY();
            final float x = Functions.snapToPixel(targetX, scale.get());
            final float y = Functions.snapToPixel(targetY, scale.get());
            e.setPos(x / scale.get(), y / scale.get());
        }
    }

    public void setVisible(boolean b) {
        entityLoadingWidget.getWidget().setVisible(b);
        tileSelectionWidget.getWidget().setVisible(b);
        if (b) {
            stage.addListener(selectionBegin);
            stage.addListener(selectionEnd);
        } else {
            stage.removeListener(selectionBegin);
            stage.removeListener(selectionEnd);
        }
    }

    public void addDevCue() {
    }

    public void saveWorld() {
        saveWorld(Path.of(WORLDS_PATH + GlobalData.currentWorld));
    }

    public void saveWorld(Path path) {
        final JSONObject worldState = Serializer.recordWorldState(worldRepresentationSupplier.get());
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
        executorService.execute(() -> {
            final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                    + "core/assets/worlds/";
            final JFileChooser chooser = new JFileChooser(relativePath);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            final JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            final int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                GlobalData.currentWorld = selectedFile.getName();
                try {
                    Serializer.loadWorldState(stage,
                            textureAtlasSupplier.get(),
                            imageAnalysisPoolSupplier.get(),
                            new JSONObject(Files.readString(selectedFile.toPath())),
                            scale.get());
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
        entityLoadingWidget.addEntity().ifPresent(e -> {
            addDragListenerToEntity(e);
            initializeModificationDialogBoxFor(e);
        });
    }

    /**
     * Helper method to add a drag listener to an entity. This drag listener adds entities to the {@link #entitySelection}.
     *
     * @param e the entity whose actor to modify by adding a drag listener.
     */
    private void addDragListenerToEntity(EntityLibgdx e) {
        e.getActor().ifPresent(
                actor -> actor.addListener(new DragListener() {
                    @Override
                    public void dragStart(InputEvent event, float x, float y, int pointer) {
                        if (developerMode.isFalse()) {
                            return;
                        }
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
