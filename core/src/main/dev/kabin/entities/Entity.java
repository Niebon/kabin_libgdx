package dev.kabin.entities;

import dev.kabin.shaders.LightSourceData;
import dev.kabin.util.collections.Id;
import dev.kabin.util.collections.LazyList;
import dev.kabin.util.helperinterfaces.JSONSerializable;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.imagemetadata.ImgMetadataDelegator;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;

/**
 * An entity represents an actor in the game that has coordinates and a sprite.
 *
 * @param <GroupType>         to which group does this entity belong. E.g {@code BACKGROUND}, {@code FOREGROUND}.
 *                            This enum helps defining the canonical ordering on entities.
 * @param <EntityType>        the type of this entity. This also defines the canonical ordering on entities.
 * @param <GraphicsParamType> the type of graphics parameters. This is a generic parameter so that one may switch graphics
 *                            dependencies.
 */
public interface Entity<

        GroupType extends Enum<GroupType>,
        EntityType extends Enum<EntityType> & Layer & GroupTyped<GroupType>,
        GraphicsParamType extends GraphicsParameters

        > extends

        ModifiableFloatCoordinates,
        Comparable<Entity<GroupType, EntityType, GraphicsParamType>>,
        ImgMetadataDelegator,
        JSONSerializable,
        Id,
        Layer {

    /**
     * Modify the layer of this entity.
     *
     * @param layer the new layer.
     * @implSpec modifies the order that the graphics is drawn relative to the group layer.
     */
    void setLayer(int layer);

    /**
     * Updates the graphics of this entity.
     *
     * @param params graphic parameters.
     */
    void updateGraphics(GraphicsParamType params);

    /**
     * Updates the physics of this entity.
     *
     * @param params physics parameters.
     */
    void updatePhysics(PhysicsParameters params);

    /**
     * A default comparing procedure for a pair of entities.
     * This is implemented as the dictionary order on:
     * <ul>
     *     <li>Comparing the {@link #typeLayer() type layer} layer of this entity.</li>
     *     <li>Comparing the {@link #layer() layer} of this entity.</li>
     *     <li>Comparing the {@link #id() id} of this.</li>
     * </ul>
     * in the given order.
     * <p>
     * This order is the order that a collection of entities should be drawn.
     * <pre> {@code
     *     var entityCollection = findCollection();
     *     entityCollection.sort(Entity::compareTo);
     *     entityCollection.forEach(e -> updateGraphics(params)); }
     * </pre>
     * For example, using this order, an entity of type
     * {@code STATIC_BACKGROUND} (typically a picture of mountains) will precede an entity
     * of type {@code ENTITY_INANIMATE} (typically an individual tree or a rock).
     *
     * @param other the entity to be compared to this.
     * @return the result of the comparison.
     */
    @Override
    default int compareTo(@NotNull Entity<GroupType, EntityType, GraphicsParamType> other) {
        final int resultCompareType = Integer.compare(typeLayer(), other.typeLayer());
        if (resultCompareType != 0) return resultCompareType;
        else {
            // Dictionary order:
            final int layerComparison = Integer.compare(layer(), other.layer());
            return (layerComparison != 0) ? layerComparison : Integer.compare(id(), other.id());
        }
    }

    default int typeLayer() {
        return getType().layer();
    }

    /**
     * @return the type of this instance.
     */
    EntityType getType();

    /**
     * @return the group type of this instance.
     */
    default GroupType getGroupType() {
        return getType().getGroupType();
    }

    default int getXAsInt() {
        return Math.round(x());
    }

    default int getYAsInt() {
        return Math.round(y());
    }

    default int getRootXAsInt() {
        return getXAsInt() - Math.round(getAvgMassCenterX());
    }

    default int getRootYAsInt() {
        return getYAsInt() - (getAvgLowestPixel() - 2);
    }

    default float getRootX() {
        return x() - getAvgMassCenterX();
    }

    default float getRootY() {
        return y() - (getAvgLowestPixel() - 2);
    }

    RectIntView graphicsNbd();

    RectIntView positionNbd();

    int getMaxPixelHeight();

    int getAvgLowestPixel();

    float getAvgMassCenterX();

    float getAvgMassCenterY();

    /**
     * @return an unmodifiable list light source data associated with this instance. This is used for shaders.
     */
    LazyList<? extends LightSourceData> getLightSourceDataList();

    /**
     * @return the points of collision that this entity makes relative to the world.
     */
    default LazyList<PointInt> collisionRelativeToWorld() {
        return LazyList.empty();
    }


}
