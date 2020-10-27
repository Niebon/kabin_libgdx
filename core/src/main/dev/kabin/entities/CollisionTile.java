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
        animatedGraphicsAsset.setCurrentAnimation(tile);
        index = Math.floorMod(parameters.get(FRAME_INDEX, Integer.class).orElseThrow(), animatedGraphicsAsset.getCurrentAnimationLength());
    }

    @Override
    public void render(SpriteBatch batch, float stateTime) {
        animatedGraphicsAsset.setX(getX());
        animatedGraphicsAsset.setY(getY());
        animatedGraphicsAsset.setScale(getScale());
        animatedGraphicsAsset.setCurrentAnimation(tile);
        animatedGraphicsAsset.renderFrameByIndex(batch, index);
        actor().setBounds(
                getX(), getY(),
                animatedGraphicsAsset.getWidth(),
                animatedGraphicsAsset.getHeight()
        );
    }

    @Override
    public void setY(float y) {
        super.setY(snapToCollisionTileGrid(y, getScale()));
    }

    @Override
    public void setX(float x) {
        super.setX(snapToCollisionTileGrid(x, getScale()));
    }

    public static float snapToCollisionTileGrid(float input, float scale){
        return Math.round(input / (TILE_SIZE * scale)) * (TILE_SIZE * scale);
    }
}
