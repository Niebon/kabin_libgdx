package dev.kabin.entities.libgdximpl.animation.enums;

import dev.kabin.entities.libgdximpl.animation.AnimationMetadataImpl;
import dev.kabin.util.Direction;

public enum Tile {
    SURFACE,
    DIAGONAL_45,
    DIAGONAL_135,
    INNER,
    INNER_45,
    INNER_135;


    public static final AnimationMetadataImpl ANIMATION_METADATA = new AnimationMetadataImpl(false, Direction.NONE, false);
}
