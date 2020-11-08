package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.animation.AnimationClass;

public class CollisionTile extends CollisionEntity {

    public static final String FRAME_INDEX = "frameIndex";
    public static final String TYPE = "type";
    public static final int TILE_SIZE = 16;
    private final AnimationClass.Tile tile;
    private final int index;

    CollisionTile(EntityParameters parameters) {
        super(parameters);
        tile = parameters.get(TYPE, AnimationClass.Tile.class).orElseThrow();
        animationPlaybackImpl.setCurrentAnimation(tile);
        index = Math.floorMod(parameters.get(FRAME_INDEX, Integer.class).orElseThrow(), animationPlaybackImpl.getCurrentAnimationLength());
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
    public void setY(float y) {
        super.setY(snapToCollisionTileGrid(Math.round(y / getScale())) * getScale());
    }

    @Override
    public void setX(float x) {
        super.setX(snapToCollisionTileGrid(Math.round(x / getScale())) * getScale());
    }

    public static int snapToCollisionTileGrid(int input){
        return (input / TILE_SIZE) * TILE_SIZE;
    }
}
