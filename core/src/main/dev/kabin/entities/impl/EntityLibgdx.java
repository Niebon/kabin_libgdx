package dev.kabin.entities.impl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.entities.Entity;

import java.util.Optional;

/**
 * All libgdx implementations for entities should extend this interface.
 */
public interface EntityLibgdx extends Entity<EntityGroup, EntityType, GraphicsParametersLibgdx> {

    /**
     * @return the actor of this entity.
     */
    Optional<Actor> getActor();

}
