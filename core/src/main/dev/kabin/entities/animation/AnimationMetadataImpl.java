package dev.kabin.entities.animation;

import dev.kabin.util.Direction;

public final class AnimationMetadataImpl implements AnimationMetadata {


    private final boolean looping;
    private final Direction direction;
    private final boolean lastFrameRepeating;

    public AnimationMetadataImpl(boolean looping, Direction direction, boolean lastFrameRepeating) {
        this.looping = looping;
        this.direction = direction;
        this.lastFrameRepeating = lastFrameRepeating;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean isLastFrameRepeating() {
        return lastFrameRepeating;
    }

}
