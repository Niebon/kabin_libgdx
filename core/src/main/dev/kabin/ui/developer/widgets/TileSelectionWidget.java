package dev.kabin.ui.developer.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.animation.AnimationBundleFactory;
import dev.kabin.entities.animation.AnimationClass;
import dev.kabin.entities.impl.*;
import dev.kabin.ui.Widget;
import dev.kabin.util.Functions;
import dev.kabin.util.Statistics;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TileSelectionWidget {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 200;
    private static final Map<AnimationClass.Tile, @NotNull Button> typeToButton = new HashMap<>();
    private static Map<AnimationClass.Tile, TextureAtlas.@NotNull AtlasRegion[]> typeToAtlasRegionsMapping;
    private final dev.kabin.ui.Widget widget;
    private String selectedAsset = "";
    private AnimationClass.Tile currentType = AnimationClass.Tile.SURFACE;
    private final Supplier<TextureAtlas> textureAtlasSupplier;
    private final Supplier<Float> mouseXRelativeToWorld;
    private final Supplier<Float> mouseYRelativeToWorld;
    private final Supplier<WorldRepresentation> worldRepresentationSupplier;
    private final Consumer<Runnable> synchronizer;


    public TileSelectionWidget(
            Supplier<TextureAtlas> atlas,
            Executor executor,
            Supplier<Float> mouseXRelativeToWorld,
            Supplier<Float> mouseYRelativeToWorld,
            Supplier<WorldRepresentation> worldRepresentationSupplier,
            Consumer<Runnable> synchronizer
    ) {
        this.textureAtlasSupplier = atlas;
        this.mouseXRelativeToWorld = mouseXRelativeToWorld;
        this.mouseYRelativeToWorld = mouseYRelativeToWorld;
        this.worldRepresentationSupplier = worldRepresentationSupplier;
        this.synchronizer = synchronizer;
        widget = new dev.kabin.ui.Widget.Builder()
                .setTitle("Tile selection widget")
                .setX(Gdx.graphics.getWidth() - WIDTH)
                .setY(0)
                .setWidth(WIDTH)
                .setHeight(HEIGHT)
                .setCollapsedWindowWidth(WIDTH)
                .build();

        var loadImageAssetButton = new TextButton("Asset", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
        loadImageAssetButton.setWidth(100);
        loadImageAssetButton.setHeight(25);
        loadImageAssetButton.setX(25);
        loadImageAssetButton.setY(25);
        loadImageAssetButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadAsset(executor);
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
    private void removeGroundTileAtCurrentMousePosition(float mouseX, float mouseY) {
        final int intX = Functions.snapToGrid(mouseX / MainGame.scaleFactor, CollisionTile.TILE_SIZE);
        final float x = intX * MainGame.scaleFactor;
        final int intY = Functions.snapToGrid(mouseY / MainGame.scaleFactor, CollisionTile.TILE_SIZE);
        final float y = intY * MainGame.scaleFactor;
        final CollisionTile matchingCt = CollisionTile.clearAt(intX, intY).orElse(null);
        if (matchingCt != null) {
            if (!worldRepresentationSupplier.get().unregisterEntity(matchingCt)) {
                throw new IllegalStateException("Tried to remove an entity which did not exist in %s.".formatted(EntityCollectionProvider.class.getName()));
            }
            matchingCt.getActor().ifPresent(Actor::remove);
            matchingCt.actionEachCollisionPoint(worldRepresentationSupplier.get()::decrementCollisionAt);
            worldRepresentationSupplier.get().getEntitiesWithinCameraBoundsCached(MainGame.camera.currentCameraBounds()).remove(matchingCt);
        } else {
            final Iterator<Entity> entityIterator = worldRepresentationSupplier.get().getEntitiesWithinCameraBoundsCached(MainGame.camera.currentCameraBounds()).iterator();
            while (entityIterator.hasNext()) {
                final Entity e = entityIterator.next();
                if (e instanceof CollisionTile && e.getX() == x && e.getY() == y) {
                    final var ct = (CollisionTile) e;
                    if (!worldRepresentationSupplier.get().unregisterEntity(e)) {
                        throw new IllegalStateException("Tried to remove an entity which did not exist in %s.".formatted(EntityCollectionProvider.class.getName()));
                    }
                    CollisionTile.clearAt(ct.getUnscaledX(), ct.getUnscaledY()).orElseThrow();
                    ct.getActor().ifPresent(Actor::remove);
                    ct.actionEachCollisionPoint(worldRepresentationSupplier.get()::decrementCollisionAt);
                    // Finally remove from cache, so that the next time this method is called, the same entity is not erased twice.
                    entityIterator.remove();
                }
            }
        }
    }

    /**
     * Finds any entity on screen. Deletes any of type {@link CollisionTile collision tile} with position matching
     * the current mouse position.
     */
    public void removeGroundTileAtCurrentMousePositionThreadLocked() {
        synchronizer.accept(
                () -> removeGroundTileAtCurrentMousePosition(mouseXRelativeToWorld.get(), mouseYRelativeToWorld.get())
        );
    }

    public void loadSettings(JSONObject settings) {
        selectedAsset = settings.getString("asset");
        currentType = AnimationClass.Tile.valueOf(settings.getString("type"));
        widget.setCollapsed(settings.getBoolean("collapsed"));


        // Finally, show content:
        typeToAtlasRegionsMapping = AnimationBundleFactory.findTypeToAtlasRegionsMapping(textureAtlasSupplier.get(), selectedAsset, AnimationClass.Tile.class);
        displaySelectTileButtons();
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("asset", selectedAsset.isEmpty() ? "ground" : selectedAsset)
                .put("type", currentType)
                .put("collapsed", widget.isCollapsed());
    }

    /**
     * This procedure removes any {@link CollisionTile collision tile} residing at the current mouse position.
     * Afterwards, a new collision tile is added at the current mouse position.
     */
    public void replaceCollisionTileAtCurrentMousePositionWithCurrentSelection() {
        synchronizer.accept(() -> {
            if (selectedAsset == null) return;
            if (currentType == null) return;
            final EntityParameters parameters = new EntityParameters.Builder()
                    .setX(mouseXRelativeToWorld.get())
                    .setY(mouseYRelativeToWorld.get())
                    .setLayer(0)
                    .setScale(MainGame.scaleFactor)
                    .setAtlasPath(selectedAsset)
                    .put(CollisionTile.FRAME_INDEX, Statistics.RANDOM.nextInt())
                    .put(CollisionTile.TILE, currentType.name())
                    .setTextureAtlas(textureAtlasSupplier.get())
                    .build();

            //System.out.println("Position: " + MouseEventUtil.getPositionRelativeToWorld());

            // Clear any collision tile resting at the new location.
            removeGroundTileAtCurrentMousePosition(parameters.x(), parameters.y());

            // Get the new instance.
            final CollisionTile newCollisionTile
                    = (CollisionTile) EntityFactory.EntityType.COLLISION_TILE.getParameterConstructor().construct(parameters);

            // Init the data.
            newCollisionTile.getActor().ifPresent(GlobalData.stage::addActor);
            worldRepresentationSupplier.get().registerEntity(newCollisionTile);

            // Add collision data.
            worldRepresentationSupplier.get().activate(Math.round(parameters.x()), Math.round(parameters.y()));
            newCollisionTile.actionEachCollisionPoint((x, y) -> {
                worldRepresentationSupplier.get().activate(x, y);
                worldRepresentationSupplier.get().incrementCollisionAt(x, y);
            });
            worldRepresentationSupplier.get().updateLocation(newCollisionTile);
            //System.out.println("Position: " + newCollisionTile.getPosition());
        });
    }

    private void loadAsset(Executor executor) {
        executor.execute(() -> {
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
                typeToAtlasRegionsMapping = AnimationBundleFactory.findTypeToAtlasRegionsMapping(textureAtlasSupplier.get(), selectedAsset, AnimationClass.Tile.class);
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

            var button = new TextButton("", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
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


    public void render(GraphicsParameters params) {
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


                SpriteBatch batch = params.getBatch();
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
