package dev.kabin.entities;

import dev.kabin.geometry.helperinterfaces.ModifiableCoordinates;
import dev.kabin.geometry.helperinterfaces.Scalable;
import org.jetbrains.annotations.NotNull;

public interface Entity extends Scalable, ModifiableCoordinates, Comparable<Entity> {


    void render(float stateTime);

    void updatePhysics();

    int getLayer();


    String getAtlasPath();

    @Override
    default int compareTo(@NotNull Entity other) {
        return Integer.compare(getLayer(), other.getLayer());
    }


}
