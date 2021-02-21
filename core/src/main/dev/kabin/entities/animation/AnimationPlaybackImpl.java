package dev.kabin.entities.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.util.Functions;
import dev.kabin.util.ExponentialSmoothener2D;
import dev.kabin.util.collections.IntToIntFunction;
import dev.kabin.util.pools.ImageAnalysisPool;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationPlaybackImpl<T extends Enum<T> & AnimationClass> implements AnimationPlayback, Disposable {

    public static final AnimationPlaybackImpl<?> MOCK_ANIMATION_PLAYBACK = new MockAnimationPlaybackImpl();
    private static final float DURATION_SECONDS = 0.1f; // 100 ms.
    private final int width, height;
    private final Map<T, Animation<TextureAtlas.AtlasRegion>> animationsMap;
    private final Array<TextureAtlas.AtlasRegion> regions;
    private final IntToIntFunction animationClassIndexToAnimationLength;
    private final Map<T, int[]> animationBlueprint;
    private AnimationClass currentAnimationClass;
    private TextureAtlas.AtlasRegion cachedTextureRegion;
    private ExponentialSmoothener2D exponentialSmoothener2D;
    private float scale;

    /**
     * A constructor for a mock instance.
     */
    private AnimationPlaybackImpl() {
        width = 0;
        height = 0;
        animationsMap = null;
        regions = null;
        animationClassIndexToAnimationLength = null;
        animationBlueprint = null;
    }

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
                        Functions::projectLeft,
                        () -> new EnumMap<>(tClass)
                )
        );
        this.animationBlueprint = animationBlueprint;
        width = regions.get(0).originalWidth;
        height = regions.get(0).originalHeight;
        cachedTextureRegion = regions.get(0);
        currentAnimationClass = tClass.getEnumConstants()[0];
        animationClassIndexToAnimationLength = new IntToIntFunction(tClass.getEnumConstants().length);
        exponentialSmoothener2D = new ExponentialSmoothener2D(0.5f, 0f, 0f);
        animationBlueprint.forEach((animClass, ints) -> animationClassIndexToAnimationLength.define(animClass.ordinal(), ints.length));
    }

    static AnimationPlaybackImpl<?> getMockAnimationPlaybackImpl() {
        return MOCK_ANIMATION_PLAYBACK;
    }

    public int getCurrentAnimationLength() {
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
        this.currentAnimationClass = animationClass;
        if (regions != null && animationBlueprint != null) {
            //noinspection unchecked
            final T currentAnimationClass = (T) this.currentAnimationClass;
            if (animationBlueprint.containsKey(currentAnimationClass)) {
                cachedTextureRegion = regions.get(animationBlueprint.get(currentAnimationClass)[0]);
            } else {
                cachedTextureRegion = regions.get(0);
            }
        }
//        cachedTextureRegion = regions != null ?
//                regions.get(animationBlueprint != null ? animationBlueprint.get(currentAnimationClass)[0] : 0) :
//                null;
    }

    @Override
    public void renderNextAnimationFrame(GraphicsParameters params) {
        float stateTime = params.getStateTime();

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

        SpriteBatch batch = params.getBatch();
        batch.begin();
        batch.draw(cachedTextureRegion, getX(), getY(), getWidth(), getHeight());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(GraphicsParameters params, int index) {
        //noinspection unchecked
        final T currentAnimationClass = (T) this.currentAnimationClass;
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
        return ImageAnalysisPool.findAnalysis(getCurrentImageAssetPath(), getCurrentImageAssetIndex());
    }

    @Override
    public void reset() {
        this.currentAnimationClass = currentAnimationClass.transitionToDefault();
    }

    @Override
    public void setSmoothParameters(float alpha, float initX, float initY) {
        exponentialSmoothener2D = new ExponentialSmoothener2D(alpha, initX, initY);
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
        return exponentialSmoothener2D.x();
    }

    @Override
    public void setX(float x) {
        exponentialSmoothener2D.appendSignalX(x);
    }

    @Override
    public float getY() {
        return exponentialSmoothener2D.y();
    }

    @Override
    public void setY(float y) {
        exponentialSmoothener2D.appendSignalY(y);
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

    static class MockAnimationPlaybackImpl extends AnimationPlaybackImpl {


        @Override
        public int getCurrentImageAssetIndex() {
            return 0;
        }

        @Override
        public ImageAnalysisPool.Analysis getPixelAnalysis() {
            return ImageAnalysisPool.Analysis.getMockInstance();
        }

        @Override
        public int getCurrentAnimationLength() {
            return 1;
        }

    }

}
