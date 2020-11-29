package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.GlobalData;
import dev.kabin.collections.Id;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.utilities.shapes.RectInt;
import dev.kabin.utilities.helperinterfaces.JSONRecordable;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.utilities.helperinterfaces.Scalable;
import dev.kabin.utilities.pools.ImageAnalysisPool;
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

    void updatePhysics();

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

    RectInt graphicsNbd();

    RectInt positionNbd();

    /**
     * Acts on an entity with the present vector field and returns the vector of the action.
     *
     * @param entity the entity to be acted on.
     * @return the point representing the vector (vx,vy) which acted on the entity.
     */
    default boolean routineActWithVectorFieldOn(@NotNull Entity entity) {
        final int x = entity.getUnscaledX();
        final int y = entity.getUnscaledY();
        for (int i = 0; i < 4; i++) {
            final float
                    vx = GlobalData.getRootComponent().getVectorFieldX(x, y + i),
                    vy = GlobalData.getRootComponent().getVectorFieldY(x, y + i);
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
    static float findLiftAboveGround(@NotNull Entity entity) {
        final int
                x = entity.getUnscaledX(),
                y = entity.getUnscaledY();
        int j = 0;
        while (GlobalData.getRootComponent().isCollisionIfNotLadderData(x, y + j)) j++;
        return entity.getScale() * j;
    }


}
