package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.animation.AnimationClass;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.points.PointInt;
import dev.kabin.utilities.points.UnmodifiablePointInt;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CollisionTile extends CollisionEntity {

    public static final String FRAME_INDEX = "frameIndex";
    public static final String TILE = "tile";
    public static final int TILE_SIZE = 16;
    private static final Map<UnmodifiablePointInt, CollisionTile> objectPool = new HashMap<>();
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
        overwriteCollisionTileAt(getUnscaledX(), getUnscaledY());
    }

    private void overwriteCollisionTileAt(int x, int y) {
        if (objectPool.containsKey(PointInt.unmodifiableOf(x, y))) {
            final CollisionTile removed = objectPool.remove(PointInt.unmodifiableOf(x, y));
            removed.removeCollisionData();
            EntityGroupProvider.unregisterEntity(removed);
            removed.getActor().ifPresent(Actor::remove);
        }
        objectPool.put(PointInt.unmodifiableOf(getUnscaledX(), getUnscaledY()), this);
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
}
