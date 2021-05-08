package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.entities.Entity;
import dev.kabin.shaders.AnchoredLightSourceData;
import dev.kabin.util.collections.LazyList;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
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
    @UnmodifiableView
    LazyList<AnchoredLightSourceData> getLightSourceDataList();

    /**
     * @return an unmodifiable map of light source data.
     */
    @UnmodifiableView
    Map<String, AnchoredLightSourceData> getLightSourceDataMap();

    /**
     * Registers the a light source data of the given name.
     *
     * @param name            the name of the light source to be registered.
     * @param lightSourceData the actual light source.
     */
    void addLightSourceData(String name, AnchoredLightSourceData lightSourceData);


    /**
     * Removes the given light source data from this instance.
     *
     * @param name the name of the light source data to remove.
     */
    void removeLightSourceData(String name);

}
