package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.entities.Entity;
import dev.kabin.shaders.AnchoredLightSourceData;

import java.util.Optional;

/**
 * All libgdx implementations for entities should extend this interface.
 */
public interface EntityLibgdx extends Entity<EntityGroup, EntityType, GraphicsParametersLibgdx> {

    /**
     * @return the actor of this entity.
     */
    Optional<Actor> getActor();

    @Override
    AnchoredLightSourceData getLightSourceData();

}
