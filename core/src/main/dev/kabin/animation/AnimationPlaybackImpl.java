package dev.kabin.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.utilities.collections.IntToIntFunction;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationPlaybackImpl<T extends Enum<T> & AnimationClass> implements AnimationPlayback, Disposable {

    private static final float DURATION_SECONDS = 0.1f; // 100 ms.
    final int width, height;
    private final Map<T, Animation<TextureAtlas.AtlasRegion>> animationsMap;
    private final Array<TextureAtlas.AtlasRegion> regions;
    float x, y, scale;
    private AnimationClass currentAnimationClass;
    private TextureAtlas.AtlasRegion cachedTextureRegion;
    private final IntToIntFunction animationClassIndexToAnimationLength;
    private final Map<T, int[]> animationBlueprint;

    public AnimationPlaybackImpl(
            Array<TextureAtlas.AtlasRegion> regions,
            Map<T, int[]> animationBlueprint,
            Class<T> tClass
    ) {
        this.regions = regions;
        this.animationsMap = animationBlueprint.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> generateAnimation(e.getValue()),
                        (i,j) -> i,
                        () -> new EnumMap<>(tClass)
                )
        );
        this.animationBlueprint = animationBlueprint;
        width = regions.get(0).originalWidth;
        height = regions.get(0).originalHeight;
        cachedTextureRegion = regions.get(0);
        currentAnimationClass = tClass.getEnumConstants()[0];
        animationClassIndexToAnimationLength = new IntToIntFunction(tClass.getEnumConstants().length);
        animationBlueprint.forEach((animClass, ints) -> animationClassIndexToAnimationLength.define(animClass.ordinal(), ints.length));
    }

    public int getCurrentAnimationLength(){
        return animationClassIndexToAnimationLength.eval(currentAnimationClass.ordinal());
    }

    public AnimationClass getCurrentAnimationType() {
        return currentAnimationClass;
    }

    public int getOriginalWidth() {
        return regions.get(0).originalWidth;
    }

    // For symmetry...
    @SuppressWarnings("unused")
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
        //noinspection unchecked
        final T currentAnimationClass = (T) this.currentAnimationClass;
        if (!animationsMap.containsKey(currentAnimationClass)) {
            return;
        }

        cachedTextureRegion = animationsMap.get(currentAnimationClass)
                .getKeyFrame(stateTime, currentAnimationClass.isLooping());

        // Switch to default if last frame is not repeating
        if (!currentAnimationClass.isLastFrameRepeating() &&
                !currentAnimationClass.isLooping() &&
                animationsMap.get(currentAnimationClass).isAnimationFinished(stateTime)) {
            this.currentAnimationClass = currentAnimationClass.transitionToDefault();
        }
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(SpriteBatch batch, int index) {
        //noinspection unchecked
        final T currentAnimationClass = (T) this.currentAnimationClass;
        cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimationClass)[index]);
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
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
        animationsMap.forEach((type, animation) -> Arrays.stream(animation.getKeyFrames())
                .forEach(r -> r.getTexture().dispose()));
        regions.forEach(r -> r.getTexture().dispose());
    }
}
