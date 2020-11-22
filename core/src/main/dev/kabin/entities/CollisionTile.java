package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.animation.AnimationClass;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.points.Point;
import dev.kabin.utilities.points.PrimitivePointInt;
import dev.kabin.utilities.points.UnmodifiablePointInt;

import java.util.HashMap;
import java.util.Map;

public class CollisionTile extends CollisionEntity {

    private static final Map<UnmodifiablePointInt, CollisionTile> objectPool = new HashMap<>();

    public static final String FRAME_INDEX = "frameIndex";
    public static final String TYPE = "type";
    public static final int TILE_SIZE = 16;
    private final AnimationClass.Tile tile;
    private final int index;

    /**
     * This constructor is removes any preexisting {@link CollisionTile} at the location specified by {@link #getUnscaledX()},
     * {@link #getUnscaledY()}.
     *
     * @param parameters constructor parameters.
     */
    CollisionTile(EntityParameters parameters) {
        super(parameters);
        tile = parameters.get(TYPE, AnimationClass.Tile.class).orElseThrow();
        animationPlaybackImpl.setCurrentAnimation(tile);
        index = Math.floorMod(parameters.get(FRAME_INDEX, Integer.class).orElseThrow(), animationPlaybackImpl.getCurrentAnimationLength());
        overwriteCollisionTileAt(getUnscaledX(), getUnscaledY());
    }

    private void overwriteCollisionTileAt(int x, int y) {
        if (objectPool.containsKey(PrimitivePointInt.newUnmodifiable(x, y))) {
            final CollisionTile removed = objectPool.remove(PrimitivePointInt.newUnmodifiable(x, y));
            removed.removeCollisionData();
            EntityGroupProvider.unregisterEntity(removed);
            removed.getActor().ifPresent(Actor::remove);
        }
        objectPool.put(PrimitivePointInt.newUnmodifiable(getUnscaledX(), getUnscaledY()), this);
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
        super.setX(Functions.snapToGrid(x / getScale(), TILE_SIZE) * getScale());
    }

    @Override
    public void setY(float y) {
        super.setY(Functions.snapToGrid(y / getScale(), TILE_SIZE) * getScale());
    }

}
