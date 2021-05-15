package dev.kabin.entities;

import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.RectangularArea;
import dev.kabin.util.pools.imagemetadata.ImageMetadata;

/**
 * An animation playbacks. Implementations should to draw to screen using some underlying renderer.
 *
 * @param <AnimationType>          An enum that models a set of animations that can be played by this animation playback.
 *                                 Could be an enum consisting of {@code SIT}, {@code WALK}.
 * @param <GraphicsParametersType> The class of parameters used in calls to render this animation playback.
 *                                 This class may depend on the graphics library that is used to render.
 */
public interface AnimationPlayback<

        AnimationType extends Enum<AnimationType>,
        GraphicsParametersType extends GraphicsParameters

        > extends
        ModifiableFloatCoordinates,
        RectangularArea {

    /**
     * @return the current animation of this animation playback.
     */
    AnimationType getCurrentAnimation();

    /**
     * Choose an animation to be played, starting form the next render call.
     *
     * @param animationEnum the animation to be played.
     */
    void setCurrentAnimation(AnimationType animationEnum);

    /**
     * @return the default animation of this animation playback.
     */
    AnimationType toDefaultAnimation(AnimationType current);

    /**
     * @return A path to the current image asset that is being played. This is a file path to an image.
     * @implSpec If the {@link #getCurrentAnimation() current animation} that is being played is an enum constant
     * labeled {@code SIT}, and the frame of the animation {@code SIT} that is being played is given by {@code index},
     * then this filepath should be {@code /dir/.../SIT_index.png}. Also, {@code index} must be equal to
     * the index returned by {@link #getCurrentImageAssetIndex()}.
     * @see #getCurrentImageAssetIndex()
     */
    String getCurrentImageAssetPath();

    /**
     * @return the index of the animation that is currently being played.
     * @see #getCurrentImageAssetPath()
     */
    int getCurrentImageAssetIndex();

    ImageMetadata getPixelAnalysis();

    /**
     * Reset this animation playback to its default from the current animation being played.
     * <p>
     * <b>Example.</b>
     * <p>
     * Consider an entity that can face two directions, left and right, and has animations {@code MOVE_LEFT} and {@code MOVE_RIGHT},
     * for moving, and {@code STATIONARY_LEFT} and {@code STATIONARY_RIGHT} while the entity is stationary. When drawn in the stationary
     * animations, the entity is drawn facing left or right.
     * <pre>
     *     O      O
     *     |      |
     *    /|      |\
     * </pre>
     * The move {@code MOVE_LEFT} transitions to the {@code STATIONARY_LEFT} animation upon a call to this method,
     * and the {@code MOVE_RIGHT} animation transitions to the {@code STATIONARY_RIGHT} animation.
     */
    default void toDefaultFromCurrent() {
        setCurrentAnimation(toDefaultAnimation(getCurrentAnimation()));
    }

    /**
     * @return the maximal pixel height among all animations.
     */
    int maxArtPixelHeight();

    int avgLowestArtPixel();

    float avgArtPixelMassCenterX();

    float avgArtPixelMassCenterY();


    /**
     * Return metadata associated with the given constant.
     *
     * @param animationEnum an animation enum.
     * @return the metadata associated with it.
     */
    AnimationMetadata metadataOf(AnimationType animationEnum);

    /**
     * Modifies the smoothing that this animation playback implements.
     *
     * @param alpha a parameter.
     */
    void setSmoothParameter(float alpha);

    /**
     * Renders the next animation frame using the given graphics parameters.
     *
     * @param params graphics parameters.
     */
    void renderNextAnimationFrame(GraphicsParametersType params);

    /**
     * Renders a specific frame by index.
     *
     * @param params graphics parameters.
	 * @param index  the index.
	 */
	void renderFrameByIndex(GraphicsParametersType params, int index);

	/**
	 * Modifies the height and width that this animation playback is drawn with.
	 *
	 * @param renderScale the value. For default scaling, use {@code 1f}.
	 */
	void setRenderScale(float renderScale);

	void reset();

}
