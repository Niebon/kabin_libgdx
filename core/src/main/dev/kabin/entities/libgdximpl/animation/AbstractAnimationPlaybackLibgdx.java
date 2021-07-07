package dev.kabin.entities.libgdximpl.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.entities.AnimationMetadata;
import dev.kabin.entities.ImageAnalysisGetter;
import dev.kabin.entities.libgdximpl.AnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.collections.IntToIntMap;
import dev.kabin.util.lambdas.BiFunction;
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
    private final IntToIntMap animationClassIndexToAnimationLength;
    private final Map<T, int[]> animationBlueprint;
    private final int maxPixelHeight;
    private final ImageAnalysisGetter imageAnalysisGetter;

    // Class variables:
    private T currentAnimation;
    private TextureAtlas.AtlasRegion cachedTextureRegion;
    private WeightedAverage2D weightedAverage2D;
    private ShaderProgram shaderProgram;
    private float stateTime = 0f;
    private final float avgMassCenterY;
    private final float avgMassCenterX;
    private final int avgLowestPixel;
    private float renderScale = 1f;

    AbstractAnimationPlaybackLibgdx(
            ImageAnalysisGetter imageAnalysisGetter,
            Array<TextureAtlas.AtlasRegion> regions,
            Map<T, int[]> animationBlueprint,
            Class<T> enumClass
    ) {
        this.imageAnalysisGetter = imageAnalysisGetter;
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("The parameter regions must be non-empty.");
        }
        this.regions = regions;
        this.animationsMap = animationBlueprint.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> generateAnimation(e.getValue()),
                        BiFunction::projectLeft,
                        () -> new EnumMap<>(enumClass)
                )
        );
        this.animationBlueprint = animationBlueprint;
        width = regions.get(0).originalWidth;
        height = regions.get(0).originalHeight;
        cachedTextureRegion = regions.get(0);
        currentAnimation = enumClass.getEnumConstants()[0];
        animationClassIndexToAnimationLength = new IntToIntMap(enumClass.getEnumConstants().length);
        weightedAverage2D = new WeightedAverage2D(0.5f);
        animationBlueprint.forEach((animClass, ints) -> animationClassIndexToAnimationLength.put(animClass.ordinal(), ints.length));

        maxPixelHeight = Arrays.stream(regions.items)
                .map(region -> imageAnalysisGetter.get(String.valueOf(region), region.index))
                .mapToInt(ImageMetadata::artHeight)
                .max().orElse(0);

        avgMassCenterX = (float) Arrays.stream(regions.items)
                .map(region -> imageAnalysisGetter.get(String.valueOf(region), region.index))
                .mapToDouble(ImageMetadata::artMassCenterX)
                .average().orElse(0);

        avgMassCenterY = (float) Arrays.stream(regions.items)
                .map(region -> imageAnalysisGetter.get(String.valueOf(region), region.index))
                .mapToDouble(ImageMetadata::artMassCenterY)
                .average().orElse(0);


        avgLowestPixel = (int) Arrays.stream(regions.items)
                .map(region -> imageAnalysisGetter.get(String.valueOf(region), region.index))
                .mapToInt(ImageMetadata::lowestArtPixel)
                .average().orElse(0);
    }

    @Override
    public int maxArtPixelHeight() {
        return maxPixelHeight;
    }

    @Override
    public int avgLowestArtPixel() {
        return avgLowestPixel;
    }

    @Override
    public float avgArtPixelMassCenterX() {
        return avgMassCenterX;
    }

    @Override
    public float avgArtPixelMassCenterY() {
        return avgMassCenterY;
    }

    public int getCurrentAnimationLength() {
        return animationClassIndexToAnimationLength.get(currentAnimation.ordinal());
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

        renderProcedure(params);
    }

    private void renderProcedure(GraphicsParametersLibgdx params) {
        final SpriteBatch batch = params.batch();
        batch.setShader(shaderProgram);
        batch.begin();
        batch.setColor(params.red(), params.green(), params.blue(), params.alpha());
        float scaleX = params.scaleX();
        float scaleY = params.scaleY();
        if (renderScale == 1f) {
            batch.draw(cachedTextureRegion, x() * scaleX, y() * scaleY, getWidth() * scaleX, getHeight() * scaleY);
        } else {
            batch.draw(cachedTextureRegion, x() * scaleX, y() * scaleY, getWidth() * scaleX * renderScale, getHeight() * scaleY * renderScale);
        }
        //batch.setColor(Color.WHITE); // Is resetting the color redundant?
        batch.end();
    }

    @Override
    public void renderFrameByIndex(GraphicsParametersLibgdx params, int index) {
        stateTime = 0f;
        cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimation)[index]);
        renderProcedure(params);
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
        return imageAnalysisGetter.get(getCurrentImageAssetPath(), getCurrentImageAssetIndex());
    }

    @Override
    public void setSmoothParameter(float alpha) {
        weightedAverage2D = new WeightedAverage2D(alpha);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float x() {
        return weightedAverage2D.x();
    }

    @Override
    public void setX(float x) {
        weightedAverage2D.appendSignalX(x);
    }

    @Override
    public float y() {
        return weightedAverage2D.y();
    }

    @Override
    public void setY(float y) {
        weightedAverage2D.appendSignalY(y);
    }

    @Override
    public void dispose() {
        // Is this redundant?
        animationsMap.forEach((type, animation) -> Arrays.stream(animation.getKeyFrames())
                .forEach(r -> r.getTexture().dispose()));
        regions.forEach(r -> r.getTexture().dispose());
    }

    public abstract Class<T> getAnimationClass();


    @Override
    public void setRenderScale(float renderScale) {
        this.renderScale = renderScale;
    }

    @Override
    public void reset() {
        stateTime = 0f;
    }
}
