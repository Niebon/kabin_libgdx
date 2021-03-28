package dev.kabin.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.collections.IntToIntFunction;
import dev.kabin.util.pools.ImageAnalysisPool;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractAnimationPlayback<E extends Enum<E>> implements AnimationPlayback<E>, Disposable {

    // Constants:
    private static final float DURATION_SECONDS = 0.1f; // 100 ms.

    // Class fields:
    private final int width, height;
    private final Map<E, Animation<TextureAtlas.AtlasRegion>> animationsMap;
    private final TextureAtlas atlas;
    private final Array<TextureAtlas.AtlasRegion> regions;
    private final IntToIntFunction animationClassIndexToAnimationLength;
    private final Map<E, int[]> animationBlueprint;
    private final int maxPixelHeight;
    private final Class<E> enumClass;


    // Class variables:
    private E currentAnimationEnum;
    private TextureAtlas.AtlasRegion cachedTextureRegion;
    private WeightedAverage2D weightedAverage2D;
    private float scale;

    public AbstractAnimationPlayback(
            TextureAtlas atlas,
            Array<TextureAtlas.AtlasRegion> regions,
            Map<E, int[]> animationBlueprint,
            Class<E> enumClass
    ) {
        this.enumClass = enumClass;
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("The parameter regions must be non-empty.");
        }
        this.atlas = atlas;
        this.regions = regions;
        this.animationsMap = animationBlueprint.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> generateAnimation(e.getValue()),
                        Functions::projectLeft,
                        () -> new EnumMap<>(enumClass)
                )
        );
        this.animationBlueprint = animationBlueprint;
        width = regions.get(0).originalWidth;
        height = regions.get(0).originalHeight;
        cachedTextureRegion = regions.get(0);
        currentAnimationEnum = enumClass.getEnumConstants()[0];
        animationClassIndexToAnimationLength = new IntToIntFunction(enumClass.getEnumConstants().length);
        weightedAverage2D = new WeightedAverage2D(0.5f);
        animationBlueprint.forEach((animClass, ints) -> animationClassIndexToAnimationLength.define(animClass.ordinal(), ints.length));

        maxPixelHeight = Arrays.stream(regions.items)
                .map(region -> ImageAnalysisPool.findAnalysis(atlas, String.valueOf(region), region.index))
                .mapToInt(ImageAnalysisPool.Analysis::getPixelHeight)
                .max().orElse(0);
    }

    @Override
    public Class<E> getAnimationEnumClass() {
        return enumClass;
    }

    @Override
    public int getMaxPixelHeight() {
        return maxPixelHeight;
    }

    public int getCurrentAnimationLength() {
        return animationClassIndexToAnimationLength.eval(currentAnimationEnum.ordinal());
    }

    @Override
    public E getCurrentAnimation() {
        return currentAnimationEnum;
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
    public void setCurrentAnimation(E animationEnum) {
        this.currentAnimationEnum = animationEnum;
        if (regions != null && animationBlueprint != null) {
            final E currentAnimationClass = this.currentAnimationEnum;
            if (animationBlueprint.containsKey(currentAnimationClass)) {
                cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimationClass)[0]);
            } else {
                cachedTextureRegion = regions.get(0);
            }
        }
    }

    @Override
    public void renderNextAnimationFrame(GraphicsParameters params) {
        float stateTime = params.getStateTime();

        final E currentAnimation = this.currentAnimationEnum;
        if (!animationsMap.containsKey(currentAnimation)) {
            return;
        }

        final AnimationMetadata animationMetadata = metadataOf(currentAnimation);
        cachedTextureRegion = animationsMap.get(currentAnimation)
                .getKeyFrame(stateTime, animationMetadata.isLooping());

        // Switch to default if last frame is not repeating
        if (!animationMetadata.isLastFrameRepeating() &&
                !animationMetadata.isLooping() &&
                animationsMap.get(currentAnimation).isAnimationFinished(stateTime)) {
            this.currentAnimationEnum = toDefaultAnimation(currentAnimation);
        }

        SpriteBatch batch = params.getBatch();
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(GraphicsParameters params, int index) {
        final E currentAnimationClass = this.currentAnimationEnum;
        cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimationClass)[index]);
        final SpriteBatch batch = params.getBatch();
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public String getCurrentImageAssetPath() {
        return String.valueOf(cachedTextureRegion);
    }

    @Override
    public int getCurrentImageAssetIndex() {
        return cachedTextureRegion.index;
    }

    @Override
    public ImageAnalysisPool.Analysis getPixelAnalysis() {
        return ImageAnalysisPool.findAnalysis(atlas, getCurrentImageAssetPath(), getCurrentImageAssetIndex());
    }

    @Override
    public void reset() {
        currentAnimationEnum = toDefaultAnimation(currentAnimationEnum);
    }

    @Override
    public void setSmoothParameter(float alpha) {
        weightedAverage2D = new WeightedAverage2D(alpha);
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
        return weightedAverage2D.x();
    }

    @Override
    public void setX(float x) {
        weightedAverage2D.appendSignalX(x);
    }

    @Override
    public float getY() {
        return weightedAverage2D.y();
    }

    @Override
    public void setY(float y) {
        weightedAverage2D.appendSignalY(y);
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
