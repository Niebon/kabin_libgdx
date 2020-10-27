package dev.kabin.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimatedGraphicsAsset<T extends Enum<T> & AnimationClass> implements AnimationPlayback, Disposable {

    private static final float DURATION_SECONDS = 0.1f; // 100 ms.
    final int width, height;
    private final Map<T, Animation<TextureAtlas.AtlasRegion>> animations;
    private final Array<TextureAtlas.AtlasRegion> regions;
    float x, y, scale;
    private AnimationClass currentAnimationClass;
    private TextureAtlas.AtlasRegion cachedTextureRegion;

    public AnimatedGraphicsAsset(
            Array<TextureAtlas.AtlasRegion> regions,
            Map<T, int[]> animations,
            Class<T> tClass
    ) {
        this.regions = regions;
        // According to https://stackoverflow.com/questions/47449635/cannot-infer-type-arguments-for-hashmap?rq=1
        // this is a bug with the eclipse compiler. Maybe libgdx compiles with the eclipse compiler underneath?
        ////noinspection Convert2Diamond: The compiler wants to know the parameter types ¯\_(ツ)_/¯
        this.animations = animations.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> generateAnimation(e.getValue()),
                        (i,j) -> i,
                        () -> new EnumMap<>(tClass)
                )
        );
        width = regions.get(0).originalWidth;
        height = regions.get(0).originalHeight;
        cachedTextureRegion = regions.get(0);
        currentAnimationClass = tClass.getEnumConstants()[0];
    }

    public AnimationClass getCurrentAnimationType() {
        return currentAnimationClass;
    }

    public int getOriginalWidth() {
        return regions.get(0).originalWidth;
    }

    public int getOriginalHeight() {
        return regions.get(0).originalHeight;
    }

    private Animation<TextureAtlas.AtlasRegion> generateAnimation(int[] indices) {
        final Array<TextureAtlas.AtlasRegion> textureArray = new Array<>(indices.length);
        for (int i : indices) {
            textureArray.add(regions.get(i));
        }
        return new Animation<>(DURATION_SECONDS, textureArray);
    }

    @Override
    public void setCurrentAnimation(AnimationClass animationClass) {
        currentAnimationClass = animationClass;
    }

    @Override
    public void renderNextAnimationFrame(SpriteBatch batch, float stateTime) {
        if (!animations.containsKey(currentAnimationClass)) {
            return;
        }

        cachedTextureRegion = animations.get(currentAnimationClass)
                .getKeyFrame(stateTime, currentAnimationClass.isLooping());

        // Switch to default if last frame is not repeating
        if (!currentAnimationClass.isLastFrameRepeating() &&
                !currentAnimationClass.isLooping() &&
                animations.get(currentAnimationClass).isAnimationFinished(stateTime)) {
            currentAnimationClass = currentAnimationClass.transitionToDefault();
        }
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(SpriteBatch batch, int index) {
        batch.begin();
        batch.draw(regions.get(index), getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public String getCurrentImageAssetPath() {
        return cachedTextureRegion.toString();
    }

    @Override
    public int getCurrentImageAssetIndex() {
        return cachedTextureRegion.index;
    }

    @Override
    public float getWidth() {
        return width * getScale();
    }


    @Override
    public float getHeight() {
        return height * getScale();
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
        // Is this redundant?
        animations.forEach((type, animation) -> Arrays.stream(animation.getKeyFrames())
                .forEach(r -> r.getTexture().dispose()));
        regions.forEach(r -> r.getTexture().dispose());
    }
}
