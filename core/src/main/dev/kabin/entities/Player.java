package dev.kabin.entities;

import dev.kabin.GlobalData;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.utilities.CollisionTangentFinder;
import dev.kabin.utilities.Direction;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.eventhandlers.KeyEventUtil;

import java.util.Optional;

public class Player extends EntitySimple {


    static Player instance;
    private final float throwMomentum;
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

    Player(EntityParameters parameters) {
        super(parameters);
        // Physics
        jumpFrame = 0;
        runSpeed = PhysicsEngine.meter * 8f;   // per seconds
        walkSpeed = PhysicsEngine.meter * 3f;   // per seconds
        jumpVel = -PhysicsEngine.meter * 7f; // per seconds
        toggleRunSpeed();

        // Throwing items
        throwMomentum = 140; // kg m/s
    }

    public static Optional<Player> getInstance() {
        return Optional.ofNullable(instance);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.PLAYER;
    }

    public void freeze() {
    }

    public void triggerFlashLight() {
    }

    public void toggleRunSpeed() {
    }

    public void toggleWalkSpeed() {
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

    public void handlePlayerInputMovementKeyboard() {
        exhaustRunnable();

        int lLast = l;
        int rLast = r;
        int uLast = u;
        int dLast = d;
        int jumpLast = jump;
        l = KeyEventUtil.getInstance().isPressed(KeyEventUtil.KeyCode.A) ? 1 : 0;
        r = KeyEventUtil.getInstance().isPressed(KeyEventUtil.KeyCode.D) ? 1 : 0;
        u = KeyEventUtil.getInstance().isPressed(KeyEventUtil.KeyCode.W) ? 1 : 0;
        d = KeyEventUtil.getInstance().isPressed(KeyEventUtil.KeyCode.S) ? 1 : 0;


        beganMoving = (rLast - lLast != r - l ^
                uLast - dLast != u - d ^
                (jumpLast == 0 && jump == 1));
    }

    private void exhaustRunnable() {
    }

    @Override
    public void updatePhysics(PhysicsParameters params) {
        handlePlayerInputMovementKeyboard();

        // Get initial conditions.
        final int xPrevUnscaled = getUnscaledX();
        final int yPrevUnscaled = getUnscaledY();
        final boolean affectedByVectorField = routineActWithVectorFieldOn(this, params::getVectorFieldX, params::getVectorFieldY);

        // Ladder movement
        if (params.isCollisionAt(xPrevUnscaled, yPrevUnscaled)) {
            jumpFrame = 0;
            vx0 = 0f;
            vy0 = 0f;

            if (inAir) inAir = false;
            inAir = true;

            dx = vAbsDividedBySquareRoot * (r - l) * PhysicsEngine.DT;
            dy = vAbsDividedBySquareRoot * (d - u) * PhysicsEngine.DT;

            // KeyEventUtil.keyD == KeyEventUtil.keyA <=> dx = 0???
            if (dx == 0) {
                dy = (dy < 0 && !params.isLadderAt(xPrevUnscaled, Math.round((getY() + dy) / getScale()))) ? 0 : dy;
            }

            dropHeldEntityIfOnLadder(xPrevUnscaled, Math.round((getY() - 8) / getScale()));
        }

        // Regular movement
        else {

            inAir = false;

            dx = vAbs * (r - l) * PhysicsEngine.DT;

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
                    vy0 = jumpVel;
                    if (affectedByVectorField) {
                        int i = 0;
                        while (params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled + i) == 0 && i < 8)
                            i++;
                        vx0 = params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled + i);
                        vy0 += params.getVectorFieldY(xPrevUnscaled, yPrevUnscaled + i);
                    }
                }
            }

            // Follow freeFall trajectory
            final float jumpTime = (jumpFrame++) * PhysicsEngine.DT;
            dy = (vy0 - PhysicsEngine.gravitationConstant * jumpTime) * PhysicsEngine.DT;
            dx = dx + vx0 * PhysicsEngine.DT;
        }


        // Check the proposed new coordinates with respect to collision data
        final int xNewUnscaled = Math.round((getX() + dx) / getScale());
        final int yNewUnscaled = Math.round((getY() + dy) / getScale());

        final boolean collisionWithFloor = (dy > 0 && params.isCollisionIfNotLadderData(xPrevUnscaled, yNewUnscaled));
        if (collisionWithFloor) {
            dy = Math.min(Entity.findLiftAboveGround(this, params::isCollisionAt), vAbs * PhysicsEngine.DT);
            vy0 = 0;
            vx0 = 0;
            jumpFrame = 0;
        }

        final boolean collisionWithCeiling = (dy < 0 && params.isCollisionIfNotLadderData(xPrevUnscaled, yNewUnscaled - getPixelHeight()));
        if (collisionWithCeiling) {
            dy = 0;
            vy0 = 0;
            vx0 = 0;
            jumpFrame = 0;
        }

        if (dx != 0) {
            final boolean hasFooting;
           /*
            This makes it so that the player is not stuck when jumping.
            The condition for the player to have footing is relaxed right after a jump.
            The numbers are found by experimentation.
            */
            {
                boolean foundCollision = false;
                for (int i = -8, len = (jumpFrame < 8) ? 8 : -8; i < len; i++) {
                    if (params.isCollisionIfNotLadderData(xNewUnscaled, yNewUnscaled + i)) {
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
                    if (params.isCollisionIfNotLadderData(xNewUnscaled, yNewUnscaled - i)) {
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
                final double angle;

                angle = CollisionTangentFinder.calculateCollisionSlope(xPrevUnscaled,
                        yPrevUnscaled,
                        Direction.valueOf(dx),
                        params::isCollisionAt);

                dy = (float) (vAbs * Math.sin(Math.toRadians(angle)) * PhysicsEngine.DT);
                dx = (float) (vAbs * Math.cos(Math.toRadians(angle)) * PhysicsEngine.DT);

            }
        }

        // Update physics
        setX(getX() + dx);
        setY(getY() + dy);

        // Update constraints
        jumpCooldown += PhysicsEngine.DT;

        // Check if player is in air.
        final int xUpdatedInt = getUnscaledX();
        final int yUpdatedInt = getUnscaledY();
        final boolean onLadder = params.isLadderAt(xUpdatedInt, yUpdatedInt);

        final boolean willSoonInterceptCollisionData;
        if (inAir && dy > 0) {
            double angle = Functions.findAngleDeg(dx, dy);
            int dxInt = (int) Math.cos(Math.toRadians(angle)) * 8;
            int dyInt = (int) Math.sin(Math.toRadians(angle)) * 8;
            willSoonInterceptCollisionData = params.isCollisionIfNotLadderData(xUpdatedInt + dxInt, yUpdatedInt + dyInt);
        } else {
            willSoonInterceptCollisionData =params.isCollisionIfNotLadderData(xUpdatedInt, yUpdatedInt + 8);
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