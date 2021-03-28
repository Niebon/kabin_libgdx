package dev.kabin.entities.animation.enums;

import dev.kabin.entities.animation.AnimationMetadataImpl;
import dev.kabin.util.Direction;

public enum Inanimate {
    DEFAULT;

    public static final AnimationMetadataImpl ANIMATION_METADATA = new AnimationMetadataImpl(false, Direction.NONE, false);

}
