package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.collections.Id;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.utilities.functioninterfaces.BiIntPredicate;
import dev.kabin.utilities.functioninterfaces.BiIntToFloatFunction;
import dev.kabin.utilities.helperinterfaces.JSONRecordable;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.utilities.helperinterfaces.Scalable;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import dev.kabin.utilities.shapes.primitive.UnmodifiableRectIntView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Entity extends
        Scalable,
        ModifiableFloatCoordinates,
        Comparable<Entity>,
        ImageAnalysisPool.Analysis.Analyzable,
        JSONRecordable,
        Id
{


    void render(SpriteBatch batch, float stateTime);

    void updatePhysics(PhysicsParameters params);

    /**
     * Parameters for entities that undergo physical interactions with their surroundings.
     */
    interface PhysicsParameters{

        /**
         * A collision check.
         * @param x horizontal coordinate. Positive points right relative to the screen.
         * @param y vertical coordinate. Positive points upwards the screen.
         * @return true iff the coordinate has collision.
         */
        boolean isCollisionAt(int x, int y);

        /**
         * A ladder data check.
         * @param x horizontal coordinate. Positive points right relative to the screen.
         * @param y vertical coordinate. Positive points upwards the screen.
         * @return true iff the coordinate has ladder data.
         */
        boolean isLadderAt(int x, int y);


        default boolean isCollisionIfNotLadderData(int x, int y){
            if (isLadderAt(x, y)) return false;
            else return (isCollisionAt(x, y));
        }

        /**
         * Horizontal vector field.
         * @param x horizontal coordinate. Positive points right relative to the screen.
         * @param y vertical coordinate. Positive points upwards the screen.
         * @return the magnitude of the vector field in horizontal direction.
         */
        float getVectorFieldX(int x, int y);

        /**
         * Vertical vector field.
         * @param x horizontal coordinate. Positive points right relative to the screen.
         * @param y vertical coordinate. Positive points upwards the screen.
         * @return the magnitude of the vector field in vertical direction.
         */
        float getVectorFieldY(int x, int y);

    }

    int getLayer();

    String getAtlasPath();

    @Override
    default int compareTo(@NotNull Entity other) {
        return Integer.compare(getLayer(), other.getLayer());
    }

    EntityFactory.EntityType getType();

    default Optional<Actor> getActor() {
        return Optional.empty();
    }

    default int getUnscaledX() {
        return Math.round(getX() / getScale());
    }

    default int getUnscaledY() {
        return Math.round(getY() / getScale());
    }

    default int getRootX(){
        return getUnscaledX() - getPixelAnalysis().getPixelMassCenterXInt();
    }

    default int getRootY() {
        return getUnscaledY() - getPixelAnalysis().getLowestPixel();
    }

    UnmodifiableRectIntView graphicsNbd();

    UnmodifiableRectIntView positionNbd();

    /**
     * Acts on an entity with the present vector field and returns the vector of the action.
     *
     * @param entity the entity to be acted on.
     * @return the point representing the vector (vx,vy) which acted on the entity.
     */
    default boolean routineActWithVectorFieldOn(@NotNull Entity entity,
                                                BiIntToFloatFunction vectorFieldX,
                                                BiIntToFloatFunction vectorFieldY) {
        final int x = entity.getUnscaledX();
        final int y = entity.getUnscaledY();
        for (int i = 0; i < 4; i++) {
            final float
                    vx = vectorFieldX.eval(x, y + i),
                    vy = vectorFieldY.eval(x, y + i);
            if (vx != 0 || vy != 0) {
                entity.setX(entity.getX() + vx * PhysicsEngine.DT);
                entity.setY(entity.getY() + vy * PhysicsEngine.DT);
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
    static float findLiftAboveGround(@NotNull Entity entity,
                                     @NotNull BiIntPredicate collisionPredicate) {
        final int
                x = entity.getUnscaledX(),
                y = entity.getUnscaledY();
        int j = 0;
        while (collisionPredicate.test(x, y + j)) j++;
        return entity.getScale() * j;
    }


}
