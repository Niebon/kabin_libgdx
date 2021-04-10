package dev.kabin.entities.libgdximpl.animation;

import dev.kabin.entities.AnimationMetadata;
import dev.kabin.util.Direction;

public record AnimationMetadataImpl(boolean looping,
                                    Direction direction,
                                    boolean lastFrameRepeating) implements AnimationMetadata {


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
