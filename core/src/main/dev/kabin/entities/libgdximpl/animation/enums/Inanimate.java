package dev.kabin.entities.libgdximpl.animation.enums;

import dev.kabin.entities.libgdximpl.animation.AnimationMetadataImpl;
import dev.kabin.util.Direction;

public enum Inanimate {
    DEFAULT,
    WIND_LEVEL1,
    WIND_LEVEL2;

    public static final AnimationMetadataImpl ANIMATION_METADATA = new AnimationMetadataImpl(false, Direction.NONE, false);

}
