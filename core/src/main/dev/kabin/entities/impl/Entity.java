package dev.kabin.entities.impl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.collections.Id;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.physics.PhysicsEngine;
import dev.kabin.util.functioninterfaces.BiIntPredicate;
import dev.kabin.util.functioninterfaces.BiIntToFloatFunction;
import dev.kabin.util.helperinterfaces.JSONSerializable;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.Scalable;
import dev.kabin.util.pools.ImageAnalysisPool;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Entity extends
        Scalable,
        ModifiableFloatCoordinates,
        Comparable<Entity>,
        ImageAnalysisPool.Analysis.Analyzable,
        JSONSerializable,
        Id
{

    void updateGraphics(GraphicsParameters params);

    void updatePhysics(PhysicsParameters params);

    void setLayer(int layer);

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

    default int getRootIntX(){
        return getUnscaledX() - getPixelAnalysis().getPixelMassCenterXInt();
    }

    default int getRootIntY() {
        return getUnscaledY() - getPixelAnalysis().getLowestPixel();
    }

    default float getRootX(){
        return getX() - getPixelMassCenterX() * getScale();
    }

    default float getRootY(){
        return getY() - (getLowestPixel() - 2) * getScale();
    }

    RectIntView graphicsNbd();

    RectIntView positionNbd();

    /**
     * Acts on an entity with the present vector field and returns the vector of the action.
     *
     * @param entity the entity to be acted on.
     * @return the point representing the vector (vx,vy) which acted on the entity.
     */
    static boolean action(@NotNull Entity entity,
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
