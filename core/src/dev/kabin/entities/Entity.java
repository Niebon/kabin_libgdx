package dev.kabin.entities;

import dev.kabin.geometry.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.geometry.helperinterfaces.Scalable;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.jetbrains.annotations.NotNull;

public interface Entity extends Scalable, ModifiableFloatCoordinates, Comparable<Entity>, ImageAnalysisPool.Analysis.Analyzable {


    void render(float stateTime);

    void updatePhysics();

    int getLayer();


    String getAtlasPath();

    @Override
    default int compareTo(@NotNull Entity other) {
        return Integer.compare(getLayer(), other.getLayer());
    }

    EntityFactory.EntityType getType();

}
