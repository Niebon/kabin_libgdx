package dev.kabin.entities.libgdximpl.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.entities.AnimationMetadata;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.collections.IntToIntFunction;
import dev.kabin.util.pools.imagemetadata.ImageMetadata;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @param <T> an enum that classifies the animations that can be played by this animation playback.
 */
public abstract class AbstractAnimationPlaybackLibgdx<T extends Enum<T>>
        implements AnimationPlaybackLibgdx<T>,
        Disposable {

    // Constants:
    private static final float DURATION_SECONDS = 0.1f; // 100 ms.

    // Class fields:
    private final int width, height;
    private final Map<T, Animation<TextureAtlas.AtlasRegion>> animationsMap;
    private final Array<TextureAtlas.AtlasRegion> regions;
    private final IntToIntFunction animationClassIndexToAnimationLength;
    private final Map<T, int[]> animationBlueprint;
    private final int maxPixelHeight;
    private final ImageAnalysisSupplier imageAnalysisSupplier;

    // Class variables:
    private T currentAnimation;
    private TextureAtlas.AtlasRegion cachedTextureRegion;
    private WeightedAverage2D weightedAverage2D;
    private ShaderProgram shaderProgram;
    private float scale;
    private float stateTime = 0f;
    private final float avgMassCenterY;
    private final float avgMassCenterX;
    private final int avgLowestPixel;

    AbstractAnimationPlaybackLibgdx(
            ImageAnalysisSupplier imageAnalysisSupplier,
            Array<TextureAtlas.AtlasRegion> regions,
            Map<T, int[]> animationBlueprint,
            Class<T> enumClass
    ) {
        this.imageAnalysisSupplier = imageAnalysisSupplier;
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("The parameter regions must be non-empty.");
        }
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
        currentAnimation = enumClass.getEnumConstants()[0];
        animationClassIndexToAnimationLength = new IntToIntFunction(enumClass.getEnumConstants().length);
        weightedAverage2D = new WeightedAverage2D(0.5f);
        animationBlueprint.forEach((animClass, ints) -> animationClassIndexToAnimationLength.define(animClass.ordinal(), ints.length));

        maxPixelHeight = Arrays.stream(regions.items)
                .map(region -> imageAnalysisSupplier.get(String.valueOf(region), region.index))
                .mapToInt(ImageMetadata::getPixelHeight)
                .max().orElse(0);

        avgMassCenterX = (float) Arrays.stream(regions.items)
                .map(region -> imageAnalysisSupplier.get(String.valueOf(region), region.index))
                .mapToDouble(ImageMetadata::getPixelMassCenterX)
                .average().orElse(0);

        avgMassCenterY = (float) Arrays.stream(regions.items)
                .map(region -> imageAnalysisSupplier.get(String.valueOf(region), region.index))
                .mapToDouble(ImageMetadata::getPixelMassCenterY)
                .average().orElse(0);


        avgLowestPixel = (int) Arrays.stream(regions.items)
                .map(region -> imageAnalysisSupplier.get(String.valueOf(region), region.index))
                .mapToInt(ImageMetadata::getLowestPixel)
                .average().orElse(0);
    }

    @Override
    public int getMaxPixelHeight() {
        return maxPixelHeight;
    }

    @Override
    public int getAvgLowestPixel() {
        return avgLowestPixel;
    }

    @Override
    public float getAvgMassCenterX() {
        return avgMassCenterX;
    }

    @Override
    public float getAvgMassCenterY() {
        return avgMassCenterY;
    }

    public int getCurrentAnimationLength() {
        return animationClassIndexToAnimationLength.eval(currentAnimation.ordinal());
    }

    @Override
    public T getCurrentAnimation() {
        return currentAnimation;
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
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
    public void setCurrentAnimation(T animation) {
        if (animation != currentAnimation) {
            stateTime = 0f;
            currentAnimation = animation;
        }
        if (regions != null && animationBlueprint != null) {
            if (animationBlueprint.containsKey(currentAnimation)) {
                cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimation)[0]);
            } else {
                cachedTextureRegion = regions.get(0);
            }
        }
    }

    @Override
    public void renderNextAnimationFrame(GraphicsParametersLibgdx params) {
        stateTime += params.timeElapsedSinceLastFrame();

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

            // The method setCurrentAnimation should be used, rather than updating the variable directly, since
            // it resets the state time of this instance.
            setCurrentAnimation(toDefaultAnimation(currentAnimation));
        }

        final SpriteBatch batch = params.batch();
        batch.setShader(shaderProgram);
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(GraphicsParametersLibgdx params, int index) {
        stateTime = 0f;
        cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimation)[index]);

        final SpriteBatch batch = params.batch();
        batch.setShader(shaderProgram);
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
    public ImageMetadata getPixelAnalysis() {
        return imageAnalysisSupplier.get(getCurrentImageAssetPath(), getCurrentImageAssetIndex());
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

    public abstract Class<T> getAnimationClass();

}
