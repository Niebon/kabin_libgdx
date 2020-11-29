package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.animation.AnimationClass;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.points.PointInt;
import dev.kabin.utilities.points.UnmodifiablePointInt;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CollisionTile extends CollisionEntity {

    public static final String FRAME_INDEX = "frameIndex";
    public static final String TILE = "tile";
    public static final int TILE_SIZE = 16;
    private static final Map<UnmodifiablePointInt, CollisionTile> objectPool = new ConcurrentHashMap<>();
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
        tile = AnimationClass.Tile.valueOf(parameters.get(TILE, String.class).orElseThrow());
        animationPlaybackImpl.setCurrentAnimation(tile);
        index = Math.floorMod(parameters.get(FRAME_INDEX, Integer.class).orElseThrow(), animationPlaybackImpl.getCurrentAnimationLength());
        if (objectPool.containsKey(PointInt.unmodifiableOf(unscaledX, unscaledY))) {
            //System.out.println("Already contained: " + unscaledX + ", " + unscaledY);
            throw new IllegalArgumentException("The position at which this collision tile was placed was already occupied. Use the clearAt method to clear.");
        }
        //System.out.println("Adding: " + unscaledX + ", " + unscaledY);
        objectPool.put(PointInt.unmodifiableOf(unscaledX, unscaledY), this);
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
        //System.out.println("Now cleaning: " + x + ", " + y);
        return Optional.ofNullable(objectPool.remove(PointInt.unmodifiableOf(x, y)));
    }

    public static Optional<CollisionTile> clearAt(float x, float y, float scaleFactor) {
        int xInt = Functions.snapToGrid(x / scaleFactor, TILE_SIZE);
        int yInt = Functions.snapToGrid(y / scaleFactor, TILE_SIZE);
        //System.out.println("Now cleaning: " + xInt + ", " + yInt);
        return Optional.ofNullable(objectPool.remove(PointInt.unmodifiableOf(
                xInt, yInt
        )));
    }


    @Override
    public void render(SpriteBatch batch, float stateTime) {
        animationPlaybackImpl.setX(getX());
        animationPlaybackImpl.setY(getY());
        animationPlaybackImpl.setScale(getScale());
        animationPlaybackImpl.setCurrentAnimation(tile);
        animationPlaybackImpl.renderFrameByIndex(batch, index);
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
    public int getRootX() {
        return getUnscaledX() - TILE_SIZE / 2;
    }

    @Override
    public int getRootY() {
        return getUnscaledY() - TILE_SIZE / 2;
    }


}
