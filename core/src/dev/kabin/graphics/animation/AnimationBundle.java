package dev.kabin.graphics.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.utilities.Direction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationBundle implements Animations, Disposable {

    private static final float DURATION_SECONDS = 0.1f; // 100 ms.

    private final SpriteBatch batch = new SpriteBatch();
    private final Map<AnimationType, Animation<TextureAtlas.AtlasRegion>> animations;
    private final Array<TextureAtlas.AtlasRegion> regions;
    float x, y, width, height, scale;
    private AnimationType currentAnimationType = AnimationType.DEFAULT_RIGHT;

    private TextureAtlas.AtlasRegion cachedTextureRegion;

    public AnimationBundle(
            Array<TextureAtlas.AtlasRegion> regions,
            Map<AnimationType, List<Integer>> animations
    ) {
        this.regions = regions;
        //noinspection Convert2Diamond: The compiler wants to know the parameter types ¯\_(ツ)_/¯
        this.animations = new EnumMap<AnimationType, Animation<TextureAtlas.AtlasRegion>> (animations.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> generateAnimation(e.getValue()))
        ));
    }

    private Animation<TextureAtlas.AtlasRegion> generateAnimation(List<Integer> indices) {
        final Array<TextureAtlas.AtlasRegion> textureArray = new Array<>(indices.size());
        for (int i : indices) {
            textureArray.add(regions.get(i));
        }
        return new Animation<>(DURATION_SECONDS, textureArray);
    }

    @Override
    public void setCurrentAnimation(AnimationType animationType) {
        currentAnimationType = animationType;
    }

    @Override
    public void renderNextAnimationFrame(float stateTime) {
        if (!animations.containsKey(currentAnimationType)) return;

        cachedTextureRegion = animations.get(currentAnimationType).getKeyFrame(stateTime, currentAnimationType.isLooping());

        // Switch to default if last frame is not repeating
        if (!currentAnimationType.isLastFrameRepeating() &&
                !currentAnimationType.isLooping() &&
                animations.get(currentAnimationType).isAnimationFinished(stateTime)) {
            currentAnimationType = currentAnimationType.getDirection() == Direction.RIGHT ? AnimationType.DEFAULT_RIGHT
                    : AnimationType.DEFAULT_LEFT;
        }
        batch.begin();
        batch.draw(cachedTextureRegion, getX() * getScale(), getY() * getScale(),
                getWidth() * getScale(), getHeight() * getScale());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(int index) {
        batch.begin();
        batch.draw(regions.get(index),
                getX() * getScale(),
                getY() * getScale(),
                getWidth() * getScale(),
                getHeight() * getScale()
        );
        batch.end();
    }

    @Override
    public String getCurrentImageAssetPath() {
        return String.valueOf(cachedTextureRegion.index);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
