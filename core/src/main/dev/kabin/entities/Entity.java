package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.collections.Id;
import dev.kabin.geometry.shapes.RectInt;
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

}
