package dev.kabin.entities.libgdximpl.animation.enums;

import dev.kabin.entities.AnimationMetadata;
import dev.kabin.entities.libgdximpl.animation.AnimationMetadataImpl;
import dev.kabin.util.Direction;
import dev.kabin.util.lambdas.BiFunction;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Animate {

    DEFAULT_LEFT(true, false, Direction.LEFT), DEFAULT_RIGHT(true, false, Direction.RIGHT),
    STANDARD1_LEFT(false, false, Direction.LEFT), STANDARD1_RIGHT(false, false, Direction.RIGHT),
    STANDARD2_LEFT(false, false, Direction.LEFT), STANDARD2_RIGHT(false, false, Direction.RIGHT),
    STANDARD3_LEFT(false, false, Direction.LEFT), STANDARD3_RIGHT(false, false, Direction.RIGHT),
    WALK_LEFT(true, false, Direction.LEFT), WALK_RIGHT(true, false, Direction.RIGHT),
    RUN_LEFT(true, false, Direction.LEFT), RUN_RIGHT(true, false, Direction.RIGHT),
    JUMP_LEFT(false, true, Direction.LEFT), JUMP_RIGHT(false, true, Direction.RIGHT),
    CLIMB(false, false, Direction.NONE), STANDARD_CLIMB(false, false, Direction.NONE);

    private static final EnumMap<dev.kabin.entities.libgdximpl.animation.enums.Animate, AnimationMetadata> METADATA_MAP = Arrays
            .stream(Animate.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    e -> new AnimationMetadataImpl(e.looping, e.direction, e.lastFrameRepeating),
                    BiFunction::projectLeft,
                    () -> new EnumMap<>(Animate.class)
            ));

    private final boolean looping, lastFrameRepeating;
    private final Direction direction;

    Animate(boolean looping, boolean lastFrameRepeating, Direction direction) {
        this.looping = looping;
        this.lastFrameRepeating = lastFrameRepeating;
        this.direction = direction;
    }

    public AnimationMetadata getMetadata() {
        return METADATA_MAP.get(this);
    }

    public Animate toDefault() {
        return switch (direction) {
            case RIGHT -> DEFAULT_RIGHT;
            case LEFT, NONE -> DEFAULT_LEFT;
        };
    }


}
