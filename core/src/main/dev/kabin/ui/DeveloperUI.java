package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import dev.kabin.GlobalData;
import dev.kabin.WorldStateRecorder;
import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.animation.AnimationClass;
import dev.kabin.animation.AnimationPlaybackImpl;
import dev.kabin.components.Component;
import dev.kabin.entities.*;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.Statistics;
import dev.kabin.utilities.eventhandlers.KeyEventUtil;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;
import dev.kabin.utilities.functioninterfaces.PrimitiveIntPairConsumer;
import dev.kabin.utilities.points.Point;
import dev.kabin.utilities.points.PointFloat;
import dev.kabin.utilities.pools.FontPool;
import dev.kabin.utilities.shapes.RectFloat;
import org.jetbrains.annotations.NotNull;
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
import static dev.kabin.Threads.THREAD_LOCK;

public class DeveloperUI {

    private static final Set<DraggedEntity> CURRENTLY_DRAGGED_ENTITIES = new HashSet<>();
    private static final EntitySelection ENTITY_SELECTION = new EntitySelection();
    private static final Executor EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final BitmapFont BITMAP_FONT_16 = FontPool.find(16);
    private static final EntityLoadingWidget ENTITY_LOADING_WIDGET = new EntityLoadingWidget();
    private static final TileSelectionWidget TILE_SELECTION_WIDGET = new TileSelectionWidget();
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
    }

    public static void addEntityToDraggedEntities(Entity e) {
        CURRENTLY_DRAGGED_ENTITIES.add(new DraggedEntity(e.getX(), e.getY(), MouseEventUtil.getMouseXRelativeToWorld(), MouseEventUtil.getMouseYRelativeToWorld(), e));
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
            e.setY(Functions.snapToPixel((de.getEntityOriginalY() + MouseEventUtil.getMouseYRelativeToWorld() - de.getInitialMouseY())));
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

    public static EntityLoadingWidget getEntityLoadingWidget() {
        return ENTITY_LOADING_WIDGET;
    }

    public static TileSelectionWidget getTileSelectionWidget() {
        return TILE_SELECTION_WIDGET;
    }

    public static class EntitySelection {

        private final Set<Entity> currentlySelectedEntities = new HashSet<>();
        private RectFloat backingRect;
        private PointFloat begin;

        void render() {
            if (begin != null) {
                float minX = Math.min(begin.x(), MouseEventUtil.getXRelativeToUI());
                float minY = Math.min(begin.y(), MouseEventUtil.getYRelativeToUI());
                float width = Math.abs(begin.x() - MouseEventUtil.getXRelativeToUI());
                float height = Math.abs(begin.y() - MouseEventUtil.getYRelativeToUI());

                float offsetX = GlobalData.camera.position.x - GlobalData.screenWidth * 0.5f;
                float offsetY = GlobalData.camera.position.y - GlobalData.screenHeight * 0.5f;
                backingRect = new RectFloat(minX + offsetX, minY + offsetY, width, height);

                GlobalData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                GlobalData.shapeRenderer.setColor(0, 1, 1, 1);
                GlobalData.shapeRenderer.rect(begin.x(), begin.y(), MouseEventUtil.getXRelativeToUI() - begin.x(),
                        MouseEventUtil.getYRelativeToUI() - begin.y());
                GlobalData.shapeRenderer.end();

                // By abuse of the word "render" include this here...
                EntityCollectionProvider.actionForEachEntityOrderedByGroup(e -> {
                    if (backingRect.contains(e.getX(), e.getY())) {
                        currentlySelectedEntities.add(e);
                    } else {
                        currentlySelectedEntities.remove(e);
                    }
                });
            }
        }

        void begin() {
            currentlySelectedEntities.clear();
            begin = Point.of(MouseEventUtil.getXRelativeToUI(), MouseEventUtil.getYRelativeToUI());
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
        private final Widget widget;
        private AnimationPlaybackImpl<?> preview = null;
        private String selectedAsset = "";
        private EntityFactory.EntityType entityType = EntityFactory.EntityType.ENTITY_SIMPLE;
        private AnimationClass.Animate animationType = AnimationClass.Animate.DEFAULT_RIGHT;
        private int layer;

        EntityLoadingWidget() {
            widget = new Widget.Builder()
                    .setTitle("Entity loading widget")
                    .setWidth(WIDTH)
                    .setHeight(HEIGHT)
                    .setCollapsedWindowWidth(WIDTH)
                    .build();

            var loadImageAssetButton = new TextButton("Asset", Widget.Builder.DEFAULT_SKIN, "default");
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
            widget.addDialogActor(loadImageAssetButton);

            var chooseEntityTypeButton = new TextButton("Entity Type", Widget.Builder.DEFAULT_SKIN, "default");
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
            widget.addDialogActor(chooseEntityTypeButton);

            var chooseAnimationTypeButton = new TextButton("Animation", Widget.Builder.DEFAULT_SKIN, "default");
            chooseAnimationTypeButton.setWidth(100);
            chooseAnimationTypeButton.setHeight(25);
            chooseAnimationTypeButton.setX(150);
            chooseAnimationTypeButton.setY(50);
            chooseAnimationTypeButton.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    showAnimationTypeBox();
                    return true;
                }
            });
            widget.addDialogActor(chooseAnimationTypeButton);

            refreshContentTableMessage();
        }

        public Widget getWidget() {
            return widget;
        }

        void refreshContentTableMessage() {
            final int maxLength = 10;
            var contentTableMessage = new Label(
                    "Asset: " +
                            (selectedAsset.length() < maxLength ? selectedAsset
                                    : (selectedAsset.substring(0, maxLength) + "...")) +
                            '\n' +
                            "Entity primitiveType: " +
                            entityType.name() +
                            '\n' +
                            "Layer: " +
                            layer +
                            '\n' +
                            "Animation: " +
                            animationType.name(), Widget.Builder.DEFAULT_SKIN);
            contentTableMessage.setAlignment(Align.left);
            contentTableMessage.setPosition(25, 80);
            widget.refreshContentTableMessage(contentTableMessage);
        }

        public void addEntity() {

            // Early exit, no asset selected.
            if (selectedAsset == null || selectedAsset.equals("")) {
                return;
            }


            EntityParameters parameters = new EntityParameters.Builder()
                    .setX(MouseEventUtil.getMouseXRelativeToWorld())
                    .setY(MouseEventUtil.getMouseYRelativeToWorld())
                    .setLayer(layer)
                    .setScale(GlobalData.scaleFactor)
                    .setAtlasPath(selectedAsset)
                    .build();


            Entity e = entityType.getParameterConstructor().construct(parameters);
            EntityCollectionProvider.registerEntity(e);
            float offsetX = e.getPixelMassCenterX() * e.getScale();
            float offsetY = e.getPixelMassCenterY() * e.getScale();
            e.setPos(e.getX() - offsetX, e.getY() - offsetY);
            e.getActor().ifPresent(GlobalData.stage::addActor);

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
                    selectedAsset = selectedFile
                            .getAbsolutePath()
                            .replace("\\", "/")
                            .replace(relativePath, "");
                    preview = AnimationBundleFactory.loadFromAtlasPath(selectedAsset);
                }
                refreshContentTableMessage();
            });
        }


        void showSelectEntityTypeBox() {
            var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            var selectBox = new SelectBox<String>(skin, "default");
            selectBox.setItems(Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name).toArray(String[]::new));
            selectBox.setSelectedIndex(entityType.ordinal());
            var dialog = new Dialog("Setting", skin);
            dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
            dialog.getContentTable().defaults().pad(10);
            dialog.getContentTable().add(selectBox);
            dialog.setSize(200, 200);
            dialog.getTitleTable().add(new TextButton("x", skin, "default"))
                    .size(20, 20)
                    .padRight(0).padTop(0);
            dialog.setModal(true);
            widget.addActor(dialog);
            dialog.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    entityType = EntityFactory.EntityType.valueOf(selectBox.getSelected());
                    widget.removeActor(dialog);
                    refreshContentTableMessage();
                }
            });
        }

        void showAnimationTypeBox() {
            var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            var selectBox = new SelectBox<String>(skin, "default");
            selectBox.setItems(Arrays.stream(AnimationClass.Animate.values()).map(Enum::name).toArray(String[]::new));
            selectBox.setSelectedIndex(entityType.ordinal());
            var dialog = new Dialog("Setting", skin);
            dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
            dialog.getContentTable().defaults().pad(10);
            dialog.getContentTable().add(selectBox);
            dialog.setSize(200, 200);
            dialog.getTitleTable().add(new TextButton("x", skin, "default"))
                    .size(20, 20)
                    .padRight(0).padTop(0);
            dialog.setModal(true);
            widget.addActor(dialog);
            dialog.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    animationType = AnimationClass.Animate.valueOf(selectBox.getSelected());
                    widget.removeActor(dialog);
                }
            });
        }

        public void setEntityType(EntityFactory.EntityType entityType) {
            this.entityType = entityType;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }

        public void render(SpriteBatch batch, float stateTime) {
            if (widget.isVisible() && !widget.isCollapsed() && preview != null) {
                if (preview.getCurrentAnimationType() != animationType) {
                    preview.setCurrentAnimation(animationType);
                }
                float scale = 4.0f * preview.getOriginalWidth() / 32;
                preview.setScale(scale);
                preview.setPos(0.75f * WIDTH + widget.getX(), widget.getY());
                preview.renderNextAnimationFrame(batch, stateTime);
            }
        }
    }

    public static class TileSelectionWidget {

        private static final int WIDTH = 600;
        private static final int HEIGHT = 200;
        private static final Map<AnimationClass.Tile, @NotNull Button> typeToButton = new HashMap<>();
        private static Map<AnimationClass.Tile, TextureAtlas.@NotNull AtlasRegion[]> typeToAtlasRegionsMapping;
        private final Widget widget;
        private String selectedAsset;
        private AnimationClass.Tile currentType;


        public TileSelectionWidget() {
            widget = new Widget.Builder()
                    .setTitle("Tile selection widget")
                    .setX(Gdx.graphics.getWidth() - WIDTH)
                    .setY(0)
                    .setWidth(WIDTH)
                    .setHeight(HEIGHT)
                    .setCollapsedWindowWidth(WIDTH)
                    .build();

            var loadImageAssetButton = new TextButton("Asset", Widget.Builder.DEFAULT_SKIN, "default");
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
            widget.addDialogActor(loadImageAssetButton);
        }

        /**
         * Procedure for removing an instance of {@link CollisionTile} at the given position, while
         * also erasing all of its state in the sense of collision data and presence in the {@link EntityCollectionProvider}.
         *
         * @param mouseX the horizontal coordinate of the given mouse position.
         * @param mouseY the vertical coordinate of the given mouse position.
         */
        private static void removeGroundTileAtCurrentMousePosition(float mouseX, float mouseY) {
            final int intX = Functions.snapToGrid(mouseX / GlobalData.scaleFactor, CollisionTile.TILE_SIZE);
            final float x = intX * GlobalData.scaleFactor;
            final int intY = Functions.snapToGrid(mouseY / GlobalData.scaleFactor, CollisionTile.TILE_SIZE);
            final float y = intY * GlobalData.scaleFactor;
            final CollisionTile matchingCt = CollisionTile.clearAt(intX, intY).orElse(null);
            if (matchingCt != null) {
                if (!EntityCollectionProvider.unregisterEntity(matchingCt)) {
                    throw new IllegalStateException("Tried to remove an entity which did not exist in %s.".formatted(EntityCollectionProvider.class.getName()));
                }
                matchingCt.getActor().ifPresent(Actor::remove);
                matchingCt.actionEachCollisionPoint(new PrimitiveIntPairConsumer() {
                                                        @Override
                                                        public void accept(int x, int y) {
                                                            GlobalData.getRootComponent().decrementCollisionAt(x, y);
                                                        }

                                                        @Override
                                                        public String toString() {
                                                            return "GlobalData.getRootComponent().decrementCollisionAt";
                                                        }
                                                    }

                );
                Component.getEntityInCameraNeighborhoodCached().remove(matchingCt);
            } else {
                final Iterator<Entity> entityIterator = Component.getEntityInCameraNeighborhoodCached().iterator();
                while (entityIterator.hasNext()) {
                    final Entity e = entityIterator.next();
                    if (e instanceof CollisionTile && e.getX() == x && e.getY() == y) {
                        final var ct = (CollisionTile) e;
                        if (!EntityCollectionProvider.unregisterEntity(e)) {
                            throw new IllegalStateException("Tried to remove an entity which did not exist in %s.".formatted(EntityCollectionProvider.class.getName()));
                        }
                        CollisionTile.clearAt(ct.getUnscaledX(), ct.getUnscaledY()).orElseThrow();
                        ct.getActor().ifPresent(Actor::remove);

                        ct.actionEachCollisionPoint(new PrimitiveIntPairConsumer() {
                                                        @Override
                                                        public void accept(int x, int y) {
                                                            GlobalData.getRootComponent().decrementCollisionAt(x, y);
                                                        }

                                                        @Override
                                                        public String toString() {
                                                            return "GlobalData.getRootComponent().decrementCollisionAt";
                                                        }
                                                    }

                        );
                        // Finally remove from cache, so that the next time this method is called, the same entity is not erased twice.
                        entityIterator.remove();
                    }
                }
            }
        }


        /**
         * Finds any entity on screen. Deletes any of type {@link CollisionTile} with position matching
         * the current mouse position.
         */
        public static void removeGroundTileAtCurrentMousePositionThreadLocked() {
            synchronized (THREAD_LOCK) {
                removeGroundTileAtCurrentMousePosition(MouseEventUtil.getMouseXRelativeToWorld(), MouseEventUtil.getMouseYRelativeToWorld());
            }
        }

        public void overrideCollisionTileAtCurrentMousePosition() {
            synchronized (THREAD_LOCK) {
                if (selectedAsset == null) return;
                if (currentType == null) return;
                final EntityParameters parameters = new EntityParameters.Builder()
                        .setX(MouseEventUtil.getMouseXRelativeToWorld())
                        .setY(MouseEventUtil.getMouseYRelativeToWorld())
                        .setLayer(0)
                        .setScale(GlobalData.scaleFactor)
                        .setAtlasPath(selectedAsset)
                        .put(CollisionTile.FRAME_INDEX, Statistics.RANDOM.nextInt())
                        .put(CollisionTile.TILE, currentType.name())
                        .build();

                //System.out.println("Position: " + MouseEventUtil.getPositionRelativeToWorld());

                // Clear any collision tile resting at the new location.
                removeGroundTileAtCurrentMousePosition(parameters.x(), parameters.y());

                // Get the new instance.
                final CollisionTile newCollisionTile = (CollisionTile) EntityFactory.EntityType.COLLISION_TILE.getParameterConstructor().construct(parameters);

                // Init the data.
                newCollisionTile.getActor().ifPresent(GlobalData.stage::addActor);
                EntityCollectionProvider.registerEntity(newCollisionTile);
                newCollisionTile.actionEachCollisionPoint(new PrimitiveIntPairConsumer() {
                                                              @Override
                                                              public void accept(int x, int y) {
                                                                  GlobalData.getRootComponent().incrementCollisionAt(x, y);
                                                              }

                                                              @Override
                                                              public String toString() {
                                                                  return "GlobalData.getRootComponent().incrementCollisionAt";
                                                              }
                                                          }

                );
                Component.updateLocation(newCollisionTile, GlobalData.getRootComponent());

                //System.out.println("Position: " + newCollisionTile.getPosition());
            }
        }

        private void loadAsset() {
            EXECUTOR_SERVICE.execute(() -> {
                final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                        + "core/assets/raw_textures/";
                final JFileChooser chooser = new JFileChooser(relativePath);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final JFrame f = new JFrame();
                f.setVisible(true);
                f.toFront();
                f.setVisible(false);
                int res = chooser.showOpenDialog(f);
                f.dispose();
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    selectedAsset = selectedFile
                            .getAbsolutePath()
                            .replace("\\", "/")
                            .replace(relativePath, "");
                    typeToAtlasRegionsMapping = AnimationBundleFactory.findTypeToAtlasRegionsMapping(selectedAsset, AnimationClass.Tile.class);
                    displaySelectTileButtons();
                }
            });
        }

        private void displaySelectTileButtons() {
            typeToButton.forEach((t, b) -> b.remove());
            typeToButton.clear();

            for (var entry : AnimationClass.Tile.values()) {
                // Distance between displayed tiles.
                int separationOffsetFactor = entry.ordinal();

                float scale = 4.0f;
                float offsetSeparation = 1 * scale;
                float width = scale * (16 + 2);
                float height = scale * (16 + 2);
                float offsetX = 75 - scale;
                float offsetY = 75 - scale;
                float x = separationOffsetFactor * (width + offsetSeparation - scale) + offsetX;
                float y = offsetY;

                var button = new TextButton("", Widget.Builder.DEFAULT_SKIN, "default");
                button.setWidth(width);
                button.setHeight(height);
                button.setX(x);
                button.setY(y);
                button.addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        System.out.println("Clicked: " + entry);
                        currentType = entry;
                        return true;
                    }
                });
                widget.addDialogActor(button);
                widget.setVisible(true);


                typeToButton.put(entry, button);
            }

        }

        public Widget getWidget() {
            return widget;
        }


        public void render(SpriteBatch batch) {
            if (widget.isVisible() && !widget.isCollapsed() && typeToAtlasRegionsMapping != null) {

                for (var entry : typeToAtlasRegionsMapping.entrySet()) {
                    // Distance between displayed tiles.
                    int separationOffsetFactor = entry.getKey().ordinal();

                    float scale = 4.0f;
                    float offsetSeparation = 2 * scale;
                    float width = scale * 16;
                    float height = scale * 16;
                    float offsetX = 75;
                    float offsetY = 75;
                    float x = widget.getX() + separationOffsetFactor * (width + offsetSeparation) + offsetX;
                    float y = widget.getY() + offsetY;

                    batch.begin();
                    batch.draw(entry.getValue()[0],
                            x,
                            y,
                            width,
                            height);
                    batch.end();
                }
            }
        }
    }

}
