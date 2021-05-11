package dev.kabin.entities.libgdximpl;

import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityPhysicsEngine;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.enums.Animate;
import dev.kabin.util.Direction;
import dev.kabin.util.Functions;
import dev.kabin.util.Statistics;
import dev.kabin.util.TangentFinder;
import dev.kabin.util.lambdas.BiIntPredicate;
import dev.kabin.util.lambdas.BiIntToFloatFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityNPC extends EntitySimple {

    // Constants
    private static final List<Animate> STANDARD_RIGHT_LIST = List.of(
            Animate.STANDARD1_RIGHT,
            Animate.STANDARD2_RIGHT,
            Animate.STANDARD3_RIGHT
    );
    private static final List<Animate> STANDARD_LEFT_LIST = List.of(
            Animate.STANDARD1_LEFT,
            Animate.STANDARD2_LEFT,
            Animate.STANDARD3_LEFT
    );
    private static final float JUMP_VEL_METERS_PER_SECONDS = 5f;
    private static final float RUN_SPEED_PER_SECONDS = 8f;
    private static final float WALK_SPEED_PER_SECONDS = 3f;


    private int jumpFrame;
    private float vx0;
    private float vy0;
    private boolean inAir;
    private float dx, dy;
    private int r, l, u, d;
    private int jump;
    private float jumpCooldown = Float.MAX_VALUE / 2f;
    private float vAbsPerSecond;
    private boolean facingRight;
    private boolean onLadder;
    private boolean running;

    EntityNPC(EntityParameters parameters) {
        super(parameters);
        // Physics
        jumpFrame = 0;
        toggleRunSpeed();
    }

    /**
     * Acts on an entity with the present vector field and returns the vector of the action.
     *
     * @param entity the entity to be acted on.
     * @return the point representing the vector (vx,vy) which acted on the entity.
     */
    public static boolean action(@NotNull Entity<?, ?, ?> entity,
                                 BiIntToFloatFunction vectorFieldX,
                                 BiIntToFloatFunction vectorFieldY,
                                 float dt) {
        final int x = entity.getXAsInt();
        final int y = entity.getYAsInt();
        for (int i = 0; i < 4; i++) {
            final float
                    vx = vectorFieldX.eval(x, y + i),
                    vy = vectorFieldY.eval(x, y + i);
            if (vx != 0 || vy != 0) {
                entity.setPos(entity.getX() + vx * dt, entity.getY() + vy * dt);
                return true;
            }
        }
        return false;
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

    public void toggleRunSpeed() {
        running = true;
        vAbsPerSecond = RUN_SPEED_PER_SECONDS;
    }

    public void toggleWalkSpeed() {
        running = false;
        vAbsPerSecond = WALK_SPEED_PER_SECONDS;
    }

    @Override
    public void updateGraphics(GraphicsParametersLibgdx params) {
        final AbstractAnimationPlaybackLibgdx<Animate> animationPlaybackImpl = getAnimationPlaybackImpl(Animate.class);
        if (animationPlaybackImpl == null) return;

        facingRight = (r == l) ? facingRight : (r == 1 && l == 0);

        // If in air
        if (inAir || jumpCooldown < 0.5) {
            if (facingRight) animationPlaybackImpl.setCurrentAnimation(Animate.JUMP_RIGHT);
            else animationPlaybackImpl.setCurrentAnimation(Animate.JUMP_LEFT);
            // If not in air
        } else {

            if (onLadder) {

                if (dx != 0 || dy != 0) animationPlaybackImpl.setCurrentAnimation(Animate.CLIMB);
                else return;

            } else {
                // If standing still
                if (dx == 0 && dy == 0) {
                    if (facingRight && !STANDARD_RIGHT_LIST.contains(animationPlaybackImpl.getCurrentAnimation())) {
                        Animate randomPick = Statistics.drawUniform(STANDARD_RIGHT_LIST, 0.005);
                        animationPlaybackImpl.setCurrentAnimation(Objects.requireNonNullElse(randomPick, Animate.DEFAULT_RIGHT));
                    } else if (!facingRight && !STANDARD_LEFT_LIST.contains(animationPlaybackImpl.getCurrentAnimation())) {
                        Animate randomPick = Statistics.drawUniform(STANDARD_LEFT_LIST, 0.005);
                        animationPlaybackImpl.setCurrentAnimation(Objects.requireNonNullElse(randomPick, Animate.DEFAULT_LEFT));
                    }
                }

                // If walking
                if (dx > 0) {
                    animationPlaybackImpl.setCurrentAnimation(running ? Animate.RUN_RIGHT : Animate.WALK_RIGHT);
                }

                if (dx < 0) {
                    animationPlaybackImpl.setCurrentAnimation(running ? Animate.RUN_LEFT : Animate.WALK_LEFT);
                }
            }
        }

        super.updateGraphics(params);
    }


    @Override
    public void updatePhysics(PhysicsParameters params) {

        // Get initial conditions.
        final int xPrevUnscaled = getXAsInt();
        final int yPrevUnscaled = getYAsInt();
        final boolean affectedByVectorField = action(this, params::getVectorFieldX, params::getVectorFieldY, params.dt());

        // Ladder movement
        if (params.isLadderAt(xPrevUnscaled, yPrevUnscaled)) {
            jumpFrame = 0;
            vx0 = 0f;
            vy0 = 0f;

            if (inAir) inAir = false;
            onLadder = true;

            dx = WALK_SPEED_PER_SECONDS * (r - l) * params.dt();
            dy = WALK_SPEED_PER_SECONDS * (d - u) * params.dt();

            if (dx == 0) {
                dy = (dy < 0 && !params.isLadderAt(xPrevUnscaled, Math.round((getY() + dy)))) ? 0 : dy;
            }

        }

        // Regular movement
        else {

            onLadder = false;

            dx = vAbsPerSecond * params.meter() * (r - l) * params.dt();

            // Handle jump input
            if (jump == 1) {
                jump = 0;
                final double jumpCooldownThreshold = 0.2;
                if (!inAir && jumpCooldown > jumpCooldownThreshold) {
                    jumpCooldown = 0;
                    jumpFrame = 0; // start jump frame
                    Optional.ofNullable(getAnimationPlaybackImpl()).ifPresent(AbstractAnimationPlaybackLibgdx::toDefaultFromCurrent);
                    if (affectedByVectorField) {
                        int i = 0;
                        while (params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled - i) == 0 && i < 8)
                            i++;
                        vx0 = params.getVectorFieldX(xPrevUnscaled, yPrevUnscaled - i);
                        vy0 = JUMP_VEL_METERS_PER_SECONDS * params.meter() + params.getVectorFieldY(xPrevUnscaled, yPrevUnscaled - i);
                    } else {
                        vy0 = JUMP_VEL_METERS_PER_SECONDS * params.meter();
                    }
                }
            }

            // Follow freeFall trajectory
            final float jumpTime = (jumpFrame++) * params.dt();
            dy = (vy0 - EntityPhysicsEngine.GRAVITATION_CONSTANT * params.meter() * jumpTime) * params.dt();
            dx = dx + vx0 * params.dt();
        }


        // Check the proposed new coordinates with respect to collision data
        final int xNewUnscaled = Math.round((getX() + dx));
        final int yNewUnscaled = Math.round((getY() + dy));

        final boolean collisionWithFloor = (dy < 0 && params.isCollisionIfNotLadderData(xPrevUnscaled, yNewUnscaled));
        if (collisionWithFloor) {
            dy = Math.min(findLiftAboveGround(getXAsInt(), getYAsInt(), params::isCollisionAt), vAbsPerSecond * params.meter() * params.dt());
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
                final double angle = TangentFinder.slope(
                        xPrevUnscaled,
                        yPrevUnscaled,
                        Direction.valueOf(dx),
                        params::isCollisionAt);

                dy = (float) (vAbsPerSecond * params.meter() * Math.sin(Math.toRadians(angle)) * params.dt());
                dx = (float) (vAbsPerSecond * params.meter() * Math.cos(Math.toRadians(angle)) * params.dt());

            }
        }

        // Update physics
        setPos(getX() + dx, getY() + dy);
        // Cached total velocity: (velocity caused by player) + (velocity caused by environment)
        float cachedVx = dx / params.dt();
        float cachedVy = dy / params.dt();


        // Update constraints
        jumpCooldown += params.dt();

        // Check if in air.
        final int xUpdatedInt = getXAsInt();
        final int yUpdatedInt = getYAsInt();
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


}
