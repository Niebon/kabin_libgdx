package dev.kabin.entities.libgdximpl.animation;

import dev.kabin.entities.AnimationPlayback;
import dev.kabin.entities.libgdximpl.GraphicsParametersLibgdx;

/**
 * All animation playbacks used for the libgdx implementation should extend this interface.
 *
 * @param <AnimationType> a set of animations.
 */
public interface AnimationPlaybackLibgdx<AnimationType extends Enum<AnimationType>> extends
        AnimationPlayback<AnimationType, GraphicsParametersLibgdx> {
}
