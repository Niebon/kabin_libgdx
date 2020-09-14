package dev.kabin.graphics;

import dev.kabin.geometry.shapes.RectDouble;
import dev.kabin.utilities.Direction;

import java.util.List;

public interface Animations {

    List<AnimationType> STANDARD_RIGHT_LIST = List.of(AnimationType.STANDARD1_RIGHT,
            AnimationType.STANDARD2_RIGHT,
            AnimationType.STANDARD3_RIGHT);
    List<AnimationType> STANDARD_LEFT_LIST = List.of(AnimationType.STANDARD1_LEFT,
            AnimationType.STANDARD2_LEFT,
            AnimationType.STANDARD3_LEFT);

    enum AnimationType {

        DEFAULT_LEFT(true, false, Direction.LEFT), DEFAULT_RIGHT(true, false, Direction.RIGHT),
        STAND_LEFT(true, false, Direction.LEFT), STAND_RIGHT(true, false, Direction.RIGHT),
        STANDARD1_LEFT(false, false, Direction.LEFT), STANDARD1_RIGHT(false, false, Direction.RIGHT),
        STANDARD2_LEFT(false, false, Direction.LEFT), STANDARD2_RIGHT(false, false, Direction.RIGHT),
        STANDARD3_LEFT(false, false, Direction.LEFT), STANDARD3_RIGHT(false, false, Direction.RIGHT),
        WALK_LEFT(true, false, Direction.LEFT), WALK_RIGHT(true, false, Direction.RIGHT),
        RUN_LEFT(true, false, Direction.LEFT), RUN_RIGHT(true, false, Direction.RIGHT),
        JUMP_LEFT(false, true, Direction.LEFT), JUMP_RIGHT(false, true, Direction.RIGHT),
        CLIMB(false, false, Direction.NONE), STANDARD_CLIMB(false, false, Direction.NONE);

        // WAVE, CHEER, LAUGH, POINT_LEFT, POINT_RIGHT, SURPRISED,
        // SIT_LEFT, SIT_RIGHT


        private final boolean looping, lastFrameRepeating;
        private final Direction direction;

        AnimationType(boolean looping, boolean lastFrameRepeating, Direction direction) {
            this.looping = looping;
            this.lastFrameRepeating = lastFrameRepeating;
            this.direction = direction;
        }

        boolean isLooping() {
            return this.looping;
        }

        public Direction getDirection() {
            return direction;
        }

        public boolean isLastFrameRepeating() {
            return lastFrameRepeating;
        }
    }

    AnimationType getAnimation();

    String getCurrentFrameName();

    List<String> getFrames();

    List<String> getAnimationFrames(AnimationType animationType);

    void addFrame(String key, RectDouble viewport);

    void removeFrame(String key);

    void showFrame(String key);

    void showFrame(int index);

    void setAnimation(AnimationType animationType, List<String> animationFrames);

    void playAnimation(AnimationType animationType);

    void nextAnimationFrame();

    boolean hasKey(String key);

    boolean hasViewport(RectDouble viewport);

    boolean hasAnimation(AnimationType animationType);

    boolean hasAnimations();

    RectDouble getViewport(String frameKey);
}
