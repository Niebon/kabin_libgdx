package dev.kabin.animation;

import dev.kabin.utilities.Direction;

public interface AnimationClass {

    default boolean isLooping() {
        return false;
    }

    default Direction getDirection() {
        return Direction.NONE;
    }

    default boolean isLastFrameRepeating() {
        return false;
    }

    AnimationClass transitionToDefault();

    int ordinal();

    enum Animate implements AnimationClass {

        DEFAULT(true, true, Direction.NONE),

        DEFAULT_LEFT(true, false, Direction.LEFT), DEFAULT_RIGHT(true, false, Direction.RIGHT),
        STAND_LEFT(true, false, Direction.LEFT), STAND_RIGHT(true, false, Direction.RIGHT),
        STANDARD1_LEFT(false, false, Direction.LEFT), STANDARD1_RIGHT(false, false, Direction.RIGHT),
        STANDARD2_LEFT(false, false, Direction.LEFT), STANDARD2_RIGHT(false, false, Direction.RIGHT),
        STANDARD3_LEFT(false, false, Direction.LEFT), STANDARD3_RIGHT(false, false, Direction.RIGHT),
        WALK_LEFT(true, false, Direction.LEFT), WALK_RIGHT(true, false, Direction.RIGHT),
        RUN_LEFT(true, false, Direction.LEFT), RUN_RIGHT(true, false, Direction.RIGHT),
        JUMP_LEFT(false, true, Direction.LEFT), JUMP_RIGHT(false, true, Direction.RIGHT),
        CLIMB(false, false, Direction.NONE), STANDARD_CLIMB(false, false, Direction.NONE);

        private final boolean looping, lastFrameRepeating;
        private final Direction direction;

        Animate(boolean looping, boolean lastFrameRepeating, Direction direction) {
            this.looping = looping;
            this.lastFrameRepeating = lastFrameRepeating;
            this.direction = direction;
        }

        @Override
        public boolean isLooping() {
            return this.looping;
        }

        @Override
        public Direction getDirection() {
            return direction;
        }

        @Override
        public boolean isLastFrameRepeating() {
            return lastFrameRepeating;
        }

        @Override
        public Animate transitionToDefault() {
            return switch (direction) {
                case LEFT -> DEFAULT_LEFT;
                case RIGHT, NONE -> DEFAULT_RIGHT;
            };
        }
    }

    enum Inanimate implements AnimationClass {
        ;

        @Override
        public AnimationClass transitionToDefault() {
            return this;
        }
    }


    enum Tile implements AnimationClass {
        SURFACE,
        DIAGONAL_45,
        DIAGONAL_135,
        INNER,
        INNER_45,
        INNER_135;

        @Override
        public AnimationClass transitionToDefault() {
            return this;
        }
    }
}
