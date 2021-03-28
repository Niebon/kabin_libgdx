package dev.kabin.entities.animation;

import dev.kabin.entities.GraphicsParameters;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.RectangularArea;
import dev.kabin.util.helperinterfaces.Scalable;
import dev.kabin.util.pools.ImageAnalysisPool;

public interface AnimationPlayback<E extends Enum<E>> extends ModifiableFloatCoordinates, RectangularArea, Scalable {

    /**
     * @return the current animation of this animation playback.
     */
    E getCurrentAnimation();

    /**
     * Choose an animation to be played, starting form the next render call.
     *
     * @param animationEnum the animation to be played.
     */
    void setCurrentAnimation(E animationEnum);

    /**
     * @return the default animation of this animation playback.
     */
    E toDefaultAnimation(E current);

    /**
     * Renders the next animation frame using the given graphics parameters.
     *
     * @param params graphics parameters.
     */
    void renderNextAnimationFrame(GraphicsParameters params);

    /**
     * Renders a specific frame by index.
     *
     * @param params graphics parameters.
     * @param index  the index.
     */
    void renderFrameByIndex(GraphicsParameters params, int index);

    String getCurrentImageAssetPath();

    int getCurrentImageAssetIndex();

    ImageAnalysisPool.Analysis getPixelAnalysis();

    void reset();

    int getMaxPixelHeight();

    /**
     * Return metadata associated with the given constant.
     *
     * @param animationEnum an animation enum.
     * @return the metadata associated with it.
     */
    AnimationMetadata metadataOf(E animationEnum);

    void setSmoothParameter(float alpha);

    Class<E> getAnimationEnumClass();
}
