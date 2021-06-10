package dev.kabin.entities.libgdximpl;

import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.enums.Animate;
import dev.kabin.util.Booleans;
import dev.kabin.util.Direction;
import dev.kabin.util.Functions;
import dev.kabin.util.TangentFinder;
import dev.kabin.util.events.*;
import dev.kabin.util.events.primitives.BoolChangeListener;
import dev.kabin.util.events.primitives.IntChangeListener;
import dev.kabin.util.lambdas.BiIntPredicate;
import dev.kabin.util.time.Cooldown;
import dev.kabin.util.time.TimedCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class Player extends EntitySimple {

    // Constants
    private static final float JUMP_VEL_METERS_PER_SECONDS = 5f;
    private static final float RUN_SPEED_PER_SECONDS = 8f;
    private static final float WALK_SPEED_PER_SECONDS = 3f;

    // Static variables:
    private static Player instance;

    // Fields:
    private final EnumEventHandler<Events.Awsd> awsdEvents = EnumEventHandlerImpl.of(Events.Awsd.class);
    private final EnumParameterizedEventHandler<PhysicsParameters, Events.Jump> jumpEvents = EnumParametrizedEventHandlerImpl.of(Events.Jump.class);
    private final IntChangeListener inputX = new IntChangeListener();
    private final IntChangeListener inputY = new IntChangeListener();
    private final IntChangeListener inputJump = new IntChangeListener();
    private final BoolChangeListener inAir = new BoolChangeListener();
    private final BoolChangeListener inputWalking = new BoolChangeListener();
    private final TimedCondition justBeganJump = new TimedCondition(true, 500L);
    private final Cooldown jumpCooldown = Cooldown.builder()
            .setDurationMillis(350L)
            .setWaitBeforeAcceptStart(300L) // This cooldown will by default wait X seconds before accepting a .start() call.
            .build(); // Make a new cooldown. Init cooldown once the player reaches the ground.

    // Class variables:
    private boolean handleInput = true;
    private int jumpFrame;

    // Cached velocity caused by environment.
    private float vx0;
    private float vy0;

    // Cached total velocity: (velocity caused by player) + (velocity caused by environment)
    private float cachedVx;
    private float cachedVy;
    private float vAbsPerSecond;
    private boolean facingRight;
    private int currentJumpLevel = 0;

    // Link player events.
    {
        // Input.
        inputX.addListener(0, () -> awsdEvents.registerEvent(Events.Awsd.HORIZONTAL_REST));
        inputX.addListener(1, () -> {
            awsdEvents.registerEvent(isUsingWalkSpeed() ? Events.Awsd.WALK_RIGHT : Events.Awsd.RUN_RIGHT);
            facingRight = true;
        });
        inputX.addListener(-1, () -> {
            awsdEvents.registerEvent(isUsingWalkSpeed() ? Events.Awsd.WALK_LEFT : Events.Awsd.RUN_LEFT);
            facingRight = false;
        });
        inputWalking.addListener(true, () -> {
            if (facingRight) awsdEvents.registerEvent(Events.Awsd.WALK_RIGHT);
            else awsdEvents.registerEvent(Events.Awsd.WALK_LEFT);
        });
        inputWalking.addListener(false, () -> {
            if (facingRight) awsdEvents.registerEvent(Events.Awsd.RUN_RIGHT);
            else awsdEvents.registerEvent(Events.Awsd.RUN_LEFT);
        });


        // State
        inAir.addListener(false, () -> jumpEvents.registerEvent(null, Events.Jump.LAND));

        // AWSD events - graphics.
        awsdEvents.addListener(Events.Awsd.RUN_LEFT, () -> getAnimPlaybackElseThrow().setCurrentAnimation(inAir.get() ? Animate.JUMP_LEFT : Animate.RUN_LEFT));
        awsdEvents.addListener(Events.Awsd.RUN_RIGHT, () -> getAnimPlaybackElseThrow().setCurrentAnimation(inAir.get() ? Animate.JUMP_RIGHT : Animate.RUN_RIGHT));
        awsdEvents.addListener(Events.Awsd.WALK_LEFT, () -> getAnimPlaybackElseThrow().setCurrentAnimation(inAir.get() ? Animate.JUMP_LEFT : Animate.WALK_LEFT));
        awsdEvents.addListener(Events.Awsd.WALK_RIGHT, () -> getAnimPlaybackElseThrow().setCurrentAnimation(inAir.get() ? Animate.JUMP_RIGHT : Animate.WALK_RIGHT));
        awsdEvents.addListener(Events.Awsd.HORIZONTAL_REST, () -> getAnimPlaybackElseThrow().setCurrentAnimation(Booleans.ternOp2(
                inAir.get(), facingRight, Animate.JUMP_RIGHT, Animate.JUMP_LEFT, Animate.DEFAULT_RIGHT, Animate.DEFAULT_LEFT
        )));


        // Jump events - graphics.
        jumpEvents.addListener(Events.Jump.JUMP, params -> {
            getAnimationPlaybackImpl().reset();
            getAnimPlaybackElseThrow().setCurrentAnimation(facingRight ? Animate.JUMP_RIGHT : Animate.JUMP_LEFT);
        });
        jumpEvents.addListener(Events.Jump.JUMP_DOUBLE, params -> {
            getAnimationPlaybackImpl().reset();
            getAnimPlaybackElseThrow().setCurrentAnimation(facingRight ? Animate.JUMP_RIGHT : Animate.JUMP_LEFT);
        });
        jumpEvents.addListener(Events.Jump.LAND, params -> {
            if (inputX.get() == 1) {
                awsdEvents.registerEvent(isUsingWalkSpeed() ? Events.Awsd.WALK_RIGHT : Events.Awsd.RUN_RIGHT);
            } else if (inputX.get() == -1) {
                awsdEvents.registerEvent(isUsingWalkSpeed() ? Events.Awsd.WALK_LEFT : Events.Awsd.RUN_LEFT);
            } else if (inputX.get() == 0) awsdEvents.registerEvent(Events.Awsd.HORIZONTAL_REST);
        });

        // Jump events - physics.
        jumpEvents.addListener(Events.Jump.JUMP, this::jumpProcedure);
        jumpEvents.addListener(Events.Jump.JUMP_DOUBLE, this::jumpProcedure);


    }

    Player(EntityParameters parameters) {
        super(parameters);
        if (instance != null) {
            throw new IllegalArgumentException("Player already exists.");
        }
        instance = this;
        // Physics
        jumpFrame = 0;
        toggleRunSpeed();
    }

    public static Optional<Player> getInstance() {
        return Optional.ofNullable(instance);
    }

    /**
     * Method for finding the displacement dy such that
     * y - dy corresponds to the first point where the given entity is placed
     * strictly above the ground/collision surface.
     */
    private static float findLiftAboveGround(int x,
                                             int y,
                                             @NotNull BiIntPredicate collisionPredicate) {
        int j = 0;
        while (collisionPredicate.test(x, y + j)) j++;
        return j;
    }

    @NotNull
    private AbstractAnimationPlaybackLibgdx<Animate> getAnimPlaybackElseThrow() {
        return Objects.requireNonNull(getAnimationPlaybackImpl(Animate.class));
    }

    private boolean isUsingWalkSpeed() {
        return inputWalking.get();
    }

    public void setHandleInput(boolean b) {
        handleInput = b;
        inputX.set(0);
        inputY.set(0);
        inputJump.set(0);
    }

    public void freeze() {
    }

    public void triggerFlashLight() {
    }

    public void toggleRunSpeed() {
        vAbsPerSecond = RUN_SPEED_PER_SECONDS;
    }

    public void toggleWalkSpeed() {
        vAbsPerSecond = WALK_SPEED_PER_SECONDS;
    }

    public void interactWithNearestIntractable() {
    }

    public Optional<Object> getHeldEntity() {
        return Optional.empty();
    }

    public void releaseHeldEntity() {
    }

    public void throwHeldEntity() {
    }

    /**
     * Helper method to set input controls.
     *
     * @param params input.
     */
    private void handlePlayerInputMovementKeyboard(PhysicsParameters params) {
        if (!handleInput) {
            return;
        }
        inputWalking.set(params.isPressed(KeyCode.SHIFT_LEFT));
        inputX.set(Booleans.toInt(params.isPressed(KeyCode.D)) - Booleans.toInt(params.isPressed(KeyCode.A)));
        inputY.set(Booleans.toInt(params.isPressed(KeyCode.W)) - Booleans.toInt(params.isPressed(KeyCode.S)));
        inputJump.set(Booleans.toInt(params.isPressed(KeyCode.SPACE)));
    }

    @Override
    public void updatePhysics(PhysicsParameters params) {
        handlePlayerInputMovementKeyboard(params);

        // Get initial conditions.
        final int xPrevAsInt = getXAsInt();
        final int yPrevAsInt = getYAsInt();

        // Ladder movement
        float dx;
        float dy;
        if (params.isLadderAt(xPrevAsInt, yPrevAsInt)) {
            jumpFrame = 0;
            vx0 = 0f;
            vy0 = 0f;

            if (inAir.get()) {
                inAir.set(false);
                jumpCooldown.start();
            }

            dx = WALK_SPEED_PER_SECONDS * inputX.get() * params.dt();
            dy = WALK_SPEED_PER_SECONDS * inputY.get() * params.dt();

            if (dx == 0) {
                dy = dy < 0 && !params.isLadderAt(xPrevAsInt, Math.round((y() + dy))) ? 0 : dy;
            }
        }

        // Regular movement
        else {

            dx = vAbsPerSecond * params.meter() * inputX.get() * params.dt();

            // Handle jump input
            if (firstJumpCondition()) {
                jumpEvents.registerEvent(params, Events.Jump.JUMP);
            } else if (secondJumpCondition()) {
                jumpEvents.registerEvent(params, Events.Jump.JUMP_DOUBLE);
            }

            // Follow freeFall trajectory
            final float jumpTime = jumpFrame++ * params.dt();
            dy = (vy0 - PhysicsParameters.GRAVITATION_CONSTANT_PER_METER * params.meter() * jumpTime) * params.dt();
            dx = dx + vx0 * params.dt();
        }


        // Check the proposed new coordinates with respect to collision data
        final int xNewUnscaled = Math.round(x() + dx);
        final int yNewUnscaled = Math.round(y() + dy);

        final boolean collisionWithFloor = (dy < 0 && params.isCollisionIfNotLadderData(xPrevAsInt, yNewUnscaled));
        if (collisionWithFloor) {
            dy = Math.min(findLiftAboveGround(getXAsInt(), getYAsInt(), params::isCollisionAt), vAbsPerSecond * params.meter() * params.dt());
            vy0 = 0;
            vx0 = 0;
            jumpFrame = 0;
        }

        final boolean collisionWithCeiling = (dy > 0 && params.isCollisionIfNotLadderData(xPrevAsInt, yNewUnscaled + artHeight()));
        if (collisionWithCeiling) {
            dy = 0;
            vy0 = 0;
            vx0 = 0;
            jumpFrame = 0;
        }

        if (dx != 0) {
            final boolean hasFooting;

            /*
             * This makes it so that the player is not stuck when jumping.
             * The condition for the player to have footing is relaxed right after a jump.
             * The numbers are found by experimentation.
             */
            {
                boolean foundCollision = false;
                for (int i = -8, len = (jumpFrame < 8) ? 8 : -8; i < len; i++) {
                    if (params.isCollisionIfNotLadderData(xNewUnscaled, yNewUnscaled - i)) {
                        foundCollision = true;
                        break;
                    }
                }
                hasFooting = foundCollision;
            }

            final boolean pathIsObstructed;
            {

                boolean foundCollision = false;
                for (int i = 4, len = artHeight(); i < len; i++) {
                    if (params.isCollisionIfNotLadderData(xNewUnscaled, yNewUnscaled + i)) {
                        foundCollision = true;
                        break;
                    }
                }
                pathIsObstructed = foundCollision;
            }
            if (pathIsObstructed) {
                dx = 0;
            } else if (hasFooting) {

                // If the path in the given direction is obstructed:
                final double angle = TangentFinder.slope(
                        xPrevAsInt,
                        yPrevAsInt,
                        Direction.valueOf(dx),
                        params::isCollisionAt);

                dy = (float) (vAbsPerSecond * params.meter() * Math.sin(Math.toRadians(angle)) * params.dt());
                dx = (float) (vAbsPerSecond * params.meter() * Math.cos(Math.toRadians(angle)) * params.dt());


            }
        }

        // Update physics
        setPos(x() + dx, y() + dy);
        cachedVx = dx / params.dt();
        cachedVy = dy / params.dt();

        // Check if in air.
        final int xUpdatedInt = getXAsInt();
        final int yUpdatedInt = getYAsInt();
        final boolean onLadder = params.isLadderAt(xUpdatedInt, yUpdatedInt);

        final boolean willSoonInterceptCollisionData;
        if (inAir.get() && dy < 0) {
            double angle = Functions.findAngleDeg(dx, dy);
            int dxInt = (int) Math.cos(Math.toRadians(angle)) * 2;
            int dyInt = (int) Math.sin(Math.toRadians(angle)) * 2;
            willSoonInterceptCollisionData = params.isCollisionIfNotLadderData(xUpdatedInt + dxInt, yUpdatedInt + dyInt);
        } else {
            willSoonInterceptCollisionData = params.isCollisionIfNotLadderData(xUpdatedInt, yUpdatedInt - 1);
        }

        if (willSoonInterceptCollisionData) {
            inAir.set(false);
            jumpCooldown.start();
            if (!justBeganJump.eval()) {
                currentJumpLevel = 0;
            }
        } else if (!onLadder) {
            inAir.set(true);
        }
    }

    /**
     * Takes physics parameters and modifies initial conditions of this player for a jump.
     *
     * @param params the physics parameters.
     */
    private void jumpProcedure(PhysicsParameters params) {
        currentJumpLevel++;
        justBeganJump.reset();
        justBeganJump.init();
        jumpCooldown.reset();
        jumpFrame = 0; // start jump frame.
        vy0 = params.getVectorFieldY(getXAsInt(), getYAsInt()) != 0
                ? JUMP_VEL_METERS_PER_SECONDS * params.meter() + params.getVectorFieldY(getXAsInt(), getYAsInt())
                : JUMP_VEL_METERS_PER_SECONDS * params.meter();
    }

    private boolean secondJumpCondition() {
        return currentJumpLevel == 1 && inputJump.get() == 1 && inputJump.last() == 0 && !justBeganJump.eval();
    }

    private boolean firstJumpCondition() {
        return currentJumpLevel == 0 && inputJump.get() == 1 && !inAir.get() && jumpCooldown.isCompleted();
    }

    public double getVx() {
        return cachedVx;
    }

    public double getVy() {
        return cachedVy;
    }

    private static class Events {

        private enum Jump {
            JUMP,
            JUMP_DOUBLE,
            LAND,
        }

        private enum Awsd {
            WALK_LEFT, WALK_RIGHT,
            RUN_LEFT, RUN_RIGHT,
            HORIZONTAL_REST,
            VERTICAL_REST,
            STOP
        }

    }

}