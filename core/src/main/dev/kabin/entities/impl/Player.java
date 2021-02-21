package dev.kabin.entities.impl;

import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.animation.AnimationClass;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.util.TangentFinder;
import dev.kabin.util.Direction;
import dev.kabin.util.Functions;
import dev.kabin.util.Statistics;
import dev.kabin.util.eventhandlers.KeyCode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Player extends EntitySimple {

    private static final List<AnimationClass.Animate> STANDARD_RIGHT_LIST = List.of(
            AnimationClass.Animate.STANDARD1_RIGHT,
            AnimationClass.Animate.STANDARD2_RIGHT,
            AnimationClass.Animate.STANDARD3_RIGHT
    );
    private static final List<AnimationClass.Animate> STANDARD_LEFT_LIST = List.of(
            AnimationClass.Animate.STANDARD1_LEFT,
            AnimationClass.Animate.STANDARD2_LEFT,
            AnimationClass.Animate.STANDARD3_LEFT
    );
    private static Player instance;
    private final float throwMomentum;
    private boolean handleInput = true;
    private int jumpFrame;
    private float vx0;
    private float vy0;
    private boolean inAir;
    private float dx, dy;
    private int r, l, u, d;
    private float vAbsDividedBySquareRoot;
    private float vAbs;
    private int jump;
    private float jumpCooldown;
    private int frameCounter;
    private float jumpVel;
    private float runSpeed;
    private float walkSpeed;
    private boolean beganMoving;
    private boolean facingRight;
    private boolean onLadder;
    private boolean running;

    Player(EntityParameters parameters) {
        super(parameters);
        if (instance != null) throw new IllegalArgumentException("Player already exists.");
        // Physics
        jumpFrame = 0;
        runSpeed = PhysicsEngine.meter * 8f;   // per seconds
        walkSpeed = PhysicsEngine.meter * 3f;   // per seconds
        jumpVel = PhysicsEngine.meter * 7f; // per seconds
        toggleRunSpeed();

        // Throwing items
        throwMomentum = 140; // kg m/s

        instance = this;
        vAbs = runSpeed;
    }

    public static Optional<Player> getInstance() {
        return Optional.ofNullable(instance);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.PLAYER;
    }

    public void setHandleInput(boolean b) {
        handleInput = b;
    }

    public void freeze() {
    }

    public void triggerFlashLight() {
    }

    public void toggleRunSpeed() {
        running = true;
        vAbs = runSpeed;
    }

    public void toggleWalkSpeed() {
        running = false;
        vAbs = walkSpeed;
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

    @Override
    public void updateGraphics(GraphicsParameters params) {

        facingRight = (r == l) ? facingRight : (r == 1 && l == 0);

        // If in air
        if (inAir) {
            if (facingRight) animationPlaybackImpl.setCurrentAnimation(AnimationClass.Animate.JUMP_RIGHT);
            else animationPlaybackImpl.setCurrentAnimation(AnimationClass.Animate.JUMP_LEFT);
            // If not in air
        } else {

            if (onLadder) {

                if (dx != 0 || dy != 0) animationPlaybackImpl.setCurrentAnimation(AnimationClass.Animate.CLIMB);
                else return;

            } else {
                // If standing still
                if (dx == 0 && dy == 0) {
                    if (facingRight && !STANDARD_RIGHT_LIST.contains(animationPlaybackImpl.getCurrentAnimationType())) {
                        AnimationClass.Animate randomPick = Statistics.drawUniform(STANDARD_RIGHT_LIST, 0.005);
                        animationPlaybackImpl.setCurrentAnimation(Objects.requireNonNullElse(randomPick, AnimationClass.Animate.DEFAULT_RIGHT));
                    } else if (!facingRight && !STANDARD_LEFT_LIST.contains(animationPlaybackImpl.getCurrentAnimationType())) {
                        AnimationClass.Animate randomPick = Statistics.drawUniform(STANDARD_LEFT_LIST, 0.005);
                        animationPlaybackImpl.setCurrentAnimation(Objects.requireNonNullElse(randomPick, AnimationClass.Animate.DEFAULT_LEFT));
                    }
                }

                // If walking
                if (dx > 0) {
                    animationPlaybackImpl.setCurrentAnimation(running ? AnimationClass.Animate.RUN_RIGHT : AnimationClass.Animate.WALK_RIGHT);
                }

                if (dx < 0) {
                    animationPlaybackImpl.setCurrentAnimation(running ? AnimationClass.Animate.RUN_LEFT : AnimationClass.Animate.WALK_LEFT);
                }
            }
        }

        super.updateGraphics(params);
    }

    /**
     * Helper method to set input controls.
     * @param params input.
     */
    private void handlePlayerInputMovementKeyboard(PhysicsParameters params) {
        exhaustRunnable();
        if (!handleInput) return;

        final int lLast = l;
        final int rLast = r;
        final int uLast = u;
        final int dLast = d;
        final int jumpLast = jump;

        l = params.isPressed(KeyCode.A) ? 1 : 0;
        r = params.isPressed(KeyCode.D) ? 1 : 0;
        u = params.isPressed(KeyCode.W) ? 1 : 0;
        d = params.isPressed(KeyCode.S) ? 1 : 0;
        jump = params.isPressed(KeyCode.SPACE) ? 1 : 0;

        beganMoving = (rLast - lLast != r - l ^
                uLast - dLast != u - d ^
                (jumpLast == 0 && jump == 1));
    }

    private void exhaustRunnable() {
    }

    @Override
    public void updatePhysics(PhysicsParameters params) {
        handlePlayerInputMovementKeyboard(params);

        // Get initial conditions.
        final int xPrevUnscaled = getUnscaledX();
        final int yPrevUnscaled = getUnscaledY();
        final boolean affectedByVectorField = Entity.action(this, params::getVectorFieldX, params::getVectorFieldY);

        // Ladder movement
        if (params.isLadderAt(xPrevUnscaled, yPrevUnscaled)) {
            jumpFrame = 0;
            vx0 = 0f;
            vy0 = 0f;

            if (inAir) inAir = false;
            onLadder = true;

            dx = vAbsDividedBySquareRoot * (r - l) * params.dt();
            dy = vAbsDividedBySquareRoot * (d - u) * params.dt();

            if (dx == 0) {
                dy = (dy < 0 && !params.isLadderAt(xPrevUnscaled, Math.round((getY() + dy) / getScale()))) ? 0 : dy;
            }

            dropHeldEntityIfOnLadder(xPrevUnscaled, Math.round((getY() - 8) / getScale()));
        }

        // Regular movement
        else {

            onLadder = false;

            dx = vAbs * (r - l) * params.dt();

            // Handle jump input
            if (jump == 1) {
                jump = 0;
                final double jumpCooldownThreshold = 0.2;

//                // Intercept jump trajectory. If the trajectory crashes with collision, then suppress the jump.
//                final boolean suppressJump;
//                {
//                    double x = getX();
//                    double y = getY();
//
//                    for (int jumpFrame = 0, len = 5; jumpFrame < len; jumpFrame++) {
//                        final double jumpTime = (jumpFrame++) * dt;
//                        double dy = (vy0 + GameData.gravitationConstant * jumpTime) * dt;
//                        double dx = this.dx + vx0 * dt;
//                        x = x + dx;
//                        y = y + dy;
//                    }
//
//                    final double jumpTime = (jumpFrame++) * dt;
//                    dy = (vy0 + GameData.gravitationConstant * jumpTime) * dt;
//                    dx = dx + vx0 * dt;
//                    final int xNewUnscaled = (int) Math.round(x / getScale());
//                    final int yNewUnscaled = (int) Math.round(y / getScale());
//
//                    suppressJump = GameData.getRootComponent().collisionIfNotLadderData(xNewUnscaled, yNewUnscaled - 2);
//                }

                if (!inAir && jumpCooldown > jumpCooldownThreshold) {
                    jumpCooldown = 0;
                    jumpFrame = 0; // start jump frame
                    frameCounter = 10; // big number => greater than play new frame threshold => next frame played is start jump frame
                    animationPlaybackImpl.reset();
                    if (affectedByVectorField) {
                        int i = 0;
                        while (params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled - i) == 0 && i < 8)
                            i++;
                        vx0 = params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled - i);
                        vy0 = jumpVel + params.getVectorFieldY(xPrevUnscaled, yPrevUnscaled - i);
                    }
                    else {
                        vy0 = jumpVel;
                    }
                }
            }

            // Follow freeFall trajectory
            final float jumpTime = (jumpFrame++) * params.dt();
            dy = (vy0 - PhysicsEngine.gravitationConstant * jumpTime) * params.dt();
            dx = dx + vx0 * params.dt();
        }


        // Check the proposed new coordinates with respect to collision data
        final int xNewUnscaled = Math.round((getX() + dx) / getScale());
        final int yNewUnscaled = Math.round((getY() + dy) / getScale());

        final boolean collisionWithFloor = (dy < 0 && params.isCollisionIfNotLadderData(xPrevUnscaled, yNewUnscaled));
        if (collisionWithFloor) {
            dy = Math.min(Entity.findLiftAboveGround(this, params::isCollisionAt), vAbs * params.dt());
            vy0 = 0;
            vx0 = 0;
            jumpFrame = 0;
        }

        final boolean collisionWithCeiling = (dy > 0 && params.isCollisionIfNotLadderData(xPrevUnscaled, yNewUnscaled + getPixelHeight()));
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
                for (int i = 4, len = getPixelHeight(); i < len; i++) {
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
                final double angle = TangentFinder.slope(xPrevUnscaled,
                        yPrevUnscaled,
                        Direction.valueOf(dx),
                        params::isCollisionAt);

                dy = (float) (vAbs * Math.sin(Math.toRadians(angle)) * params.dt());
                dx = (float) (vAbs * Math.cos(Math.toRadians(angle)) * params.dt());

                System.out.println(angle);
            }
        }

        // Update physics
        setX(getX() + dx);
        setY(getY() + dy);

        // Update constraints
        jumpCooldown += params.dt();

        // Check if player is in air.
        final int xUpdatedInt = getUnscaledX();
        final int yUpdatedInt = getUnscaledY();
        final boolean onLadder = params.isLadderAt(xUpdatedInt, yUpdatedInt);

        final boolean willSoonInterceptCollisionData;
        if (inAir && dy < 0) {
            double angle = Functions.findAngleDeg(dx, dy);
            int dxInt = (int) Math.cos(Math.toRadians(angle)) * 8;
            int dyInt = (int) Math.sin(Math.toRadians(angle)) * 8;
            willSoonInterceptCollisionData = params.isCollisionIfNotLadderData(xUpdatedInt + dxInt, yUpdatedInt + dyInt);
        } else {
            willSoonInterceptCollisionData = params.isCollisionIfNotLadderData(xUpdatedInt, yUpdatedInt - 8);
        }

        if (willSoonInterceptCollisionData) {
            inAir = false;
        } else if (!onLadder && !affectedByVectorField) {
            inAir = true;
        }
    }

    private void dropHeldEntityIfOnLadder(int xPrevUnscaled, int round) {

    }
}