package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.GroupTyped;
import dev.kabin.entities.Layer;
import dev.kabin.entities.libgdximpl.animation.enums.Animate;
import dev.kabin.entities.libgdximpl.animation.enums.Inanimate;
import dev.kabin.entities.libgdximpl.animation.enums.Tile;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;

/**
 * This enum is a converter class that converts enum constants that represent entity types to actual implementations.
 */
public enum EntityType implements Layer, GroupTyped<EntityGroup> {
    //BEAR(Bear::new, Bear::new, EntityGroupProvider.Type.FOCAL_POINT),
    //CAT(Cat::new, Cat::new, EntityGroupProvider.Type.FOCAL_POINT),
    /**
     * An inanimate entity with collision data.
     */
    COLLISION_ENTITY(CollisionEntity::new, EntityGroup.FOCAL_POINT, Inanimate.class),

    /**
     * A tile with collision.
     *
     * @see Tile
     */
    COLLISION_TILE(CollisionTile::new, EntityGroup.FOREGROUND, Tile.class),
    //COLLISION_ENTITY_MOVABLE(CollisionEntityMovable::new, CollisionEntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //COLLISION_ENTITY_THROWABLE(CollisionEntityThrowable::new, CollisionEntityThrowable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //ENTITY_BACKGROUND(EntityBackground::new, EntityBackground::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND),
    //ENTITY_BACKGROUND_LAYER_2(EntityBackgroundLayer2::new, EntityBackgroundLayer2::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND_LAYER_2),
    //ENTITY_FOREGROUND(EntityForeground::new, EntityForeground::newFromMouseClick, EntityGroupProvider.Type.FOREGROUND),
    //ENTITY_MOVABLE(EntityMovable::new, EntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //ENTITY_LUMINATING(EntityLuminating::new, EntityLuminating::new, EntityGroupProvider.Type.FOCAL_POINT),
    ENTITY_ANIMATE(EntitySimple::new, EntityGroup.FOCAL_POINT, Animate.class),
    ENTITY_INANIMATE(EntitySimple::new, EntityGroup.FOCAL_POINT, Inanimate.class),

    //ENTITY_THROWABLE(EntityThrowable::new, EntityThrowable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //FOX(Fox::new, Fox::new, EntityGroupProvider.Type.FOCAL_POINT),
    //GROUND(Ground::new, Ground::new, null),
    //GROUND_TILE(GroundTile::new,  EntityGroupProvider.Type.GROUND),
    //LADDER(Ladder::new, Ladder::new, EntityGroupProvider.Type.FOCAL_POINT),
    //MAP_CONNECTOR(MapConnector::new, MapConnector::new, null),
    //OON(Moon::new, Moon::new, EntityGroupProvider.Type.SKY),
    PLAYER(Player::new, EntityGroup.FOCAL_POINT, Animate.class),
    //STARS(Stars::new, Stars::new, EntityGroupProvider.Type.SKY),
    STATIC_BACKGROUND(StaticBackground::new, EntityGroup.STATIC_BACKGROUND, Inanimate.class),
    //SKY(Sky::new, null, EntityGroupProvider.Type.SKY),
    //SHORTCUT(Shortcut::new, Shortcut::new, EntityGroupProvider.Type.FOCAL_POINT);
    ;

    private final EntityConstructor entityConstructor;
    private final EntityGroup groupEntityGroup;
    private final Class<? extends Enum<?>> animationClass;

    EntityType(EntityConstructor entityConstructor,
               EntityGroup groupEntityGroup,
               Class<? extends Enum<?>> animationClass) {
        this.entityConstructor = entityConstructor;
        this.groupEntityGroup = groupEntityGroup;
        this.animationClass = animationClass;
    }

    @Override
    public EntityGroup getGroupType() {
        return groupEntityGroup;
    }

    public Class<? extends Enum<?>> animationClass() {
        return animationClass;
    }

    @Override
    public int getLayer() {
        return groupEntityGroup.getLayer();
    }


    /**
     * This inner class acts as a factory for entity type.
     */
    public static class Factory {

        /**
         * An entity constructor that takes a set of {@link EntityParameters} and creates a new instance of {@link EntityLibgdx}.
         *
         * @param type the type of the entity to be created.
         * @return a new instance of {@link EntityLibgdx} matching the parameters.
         */
        public static EntityConstructor parameterConstructorOf(EntityType type) {
            return type.entityConstructor;
        }

        /**
         * An entity constructor that takes json and constructs a a new instance of {@link EntityLibgdx}.
         *
         * @param type                    the type of the entity to be created.
         * @param imageMetadataPoolLibgdx the metadata pool that caches entity image data.
         * @param textureAtlas            the atlas from where entity textures are fetched.
         * @return a new instance of {@link EntityLibgdx} matching the parameters.
         */
        public static JSONConstructor JSONConstructorOf(EntityType type,
                                                        TextureAtlas textureAtlas,
                                                        ImageMetadataPoolLibgdx imageMetadataPoolLibgdx,
                                                        float scale) {
            return json -> {
                final EntityParameters build = EntityParameters
                        .builder(json, scale)
                        .setEntityType(type)
                        .setImageAnalysisPool(imageMetadataPoolLibgdx)
                        .setTextureAtlas(textureAtlas).build();
                return type.entityConstructor.construct(build);
            };
        }
    }

}
