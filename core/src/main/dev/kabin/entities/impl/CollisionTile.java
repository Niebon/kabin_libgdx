package dev.kabin.entities.impl;

import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.animation.AnimationClass;
import dev.kabin.util.Functions;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.points.ImmutablePointInt;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CollisionTile extends CollisionEntity {

    public static final String FRAME_INDEX = "frameIndex";
    public static final String TILE = "tile";
    public static final int TILE_SIZE = 16;
    private static final Map<ImmutablePointInt, CollisionTile> objectPool = new ConcurrentHashMap<>();
    private final AnimationClass.Tile tile;
    private final int index;
    private int unscaledX;
    private int unscaledY;

    /**
     * This constructor is removes any preexisting {@link CollisionTile} at the location specified by {@link #getUnscaledX()},
     * {@link #getUnscaledY()}.
     *
     * @param parameters constructor parameters.
     */
    CollisionTile(EntityParameters parameters) {
        super(parameters);
        tile = AnimationClass.Tile.valueOf(parameters.<String>getMaybe(TILE).orElseThrow());
        animationPlaybackImpl.setCurrentAnimation(tile);
        index = Math.floorMod(parameters.<Integer>getMaybe(FRAME_INDEX).orElseThrow(), animationPlaybackImpl.getCurrentAnimationLength());
        if (objectPool.containsKey(PointInt.immutable(unscaledX, unscaledY))) {
            throw new IllegalArgumentException("The position at which this collision tile was placed was already occupied. Use the clearAt method to clear.");
        }
        objectPool.put(PointInt.immutable(unscaledX, unscaledY), this);
        animationPlaybackImpl.setSmoothParameters(1,0);
    }

    public int getIndex() {
        return index;
    }

    /**
     * Clears any {@link CollisionTile} with unscaled coordinates x,y.
     *
     * @param x horizontal coordinate.
     * @param y vertical coordinate.
     * @return if the position was occupied, clears it and returns the occupant.
     */
    public static Optional<CollisionTile> clearAt(int x, int y) {
        return Optional.ofNullable(objectPool.remove(PointInt.immutable(x, y)));
    }


    @Override
    public void updateGraphics(GraphicsParameters params) {
        animationPlaybackImpl.setX(getX() - getPixelMassCenterX() * getScale());
        animationPlaybackImpl.setY(getY() - (getPixelMassCenterY() - 1) * getScale());
        animationPlaybackImpl.setScale(getScale());
        animationPlaybackImpl.setCurrentAnimation(tile);
        animationPlaybackImpl.renderFrameByIndex(params, index);
        actor().setBounds(
                getX(), getY(),
                animationPlaybackImpl.getWidth(),
                animationPlaybackImpl.getHeight()
        );
    }

    @Override
    public void setX(float x) {
        unscaledX = Functions.snapToGrid(x / getScale(), TILE_SIZE);
        super.setX(unscaledX * getScale());
    }

    @Override
    public int getUnscaledX() {
        return unscaledX;
    }

    @Override
    public void setY(float y) {
        unscaledY = Functions.snapToGrid(y / getScale(), TILE_SIZE);
        super.setY(unscaledY * getScale());
    }

    @Override
    public int getUnscaledY() {
        return unscaledY;
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.COLLISION_TILE;
    }

    @Override
    public JSONObject toJSONObject() {
        return super.toJSONObject()
                .put(FRAME_INDEX, index)
                .put(TILE, tile.name());
    }


    @Override
    public int getRootIntX() {
        return getUnscaledX() - TILE_SIZE / 2;
    }

    @Override
    public int getRootIntY() {
        return getUnscaledY() - TILE_SIZE / 2;
    }

    @Override
    public float getPixelMassCenterX() {
        return 0.5f * TILE_SIZE;
    }

    @Override
    public float getPixelMassCenterY() {
        return 0.5f * TILE_SIZE;
    }
}
