package dev.kabin.entities.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.GroupTyped;
import dev.kabin.entities.Layer;
import dev.kabin.entities.impl.animation.enums.Animate;
import dev.kabin.entities.impl.animation.enums.Inanimate;
import dev.kabin.entities.impl.animation.enums.Tile;

/**
 * This enum is a converter class that converts enum constants to actual implementations.
 */
public enum EntityType implements Layer, GroupTyped<EntityGroup> {
    //BEAR(Bear::new, Bear::new, EntityGroupProvider.Type.FOCAL_POINT),
    //CAT(Cat::new, Cat::new, EntityGroupProvider.Type.FOCAL_POINT),
    COLLISION_ENTITY(CollisionEntityInanimate::new, EntityGroup.FOCAL_POINT, Inanimate.class),
    COLLISION_TILE(CollisionTile::new, EntityGroup.FOREGROUND, Tile.class),
    //COLLISION_ENTITY_MOVABLE(CollisionEntityMovable::new, CollisionEntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //COLLISION_ENTITY_THROWABLE(CollisionEntityThrowable::new, CollisionEntityThrowable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //ENTITY_BACKGROUND(EntityBackground::new, EntityBackground::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND),
    //ENTITY_BACKGROUND_LAYER_2(EntityBackgroundLayer2::new, EntityBackgroundLayer2::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND_LAYER_2),
    //ENTITY_FOREGROUND(EntityForeground::new, EntityForeground::newFromMouseClick, EntityGroupProvider.Type.FOREGROUND),
    //ENTITY_MOVABLE(EntityMovable::new, EntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
    //ENTITY_LUMINATING(EntityLuminating::new, EntityLuminating::new, EntityGroupProvider.Type.FOCAL_POINT),
    ENTITY_ANIMATE(EntityAnimate::new, EntityGroup.FOCAL_POINT, Animate.class),
    ENTITY_INANIMATE(EntityInanimate::new, EntityGroup.FOCAL_POINT, Inanimate.class),

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

    public JsonConstructor getJsonConstructor(TextureAtlas textureAtlas, float scale) {
        return jsonObject -> entityConstructor.construct(new EntityParameters.Builder(jsonObject, scale).setTextureAtlas(textureAtlas).build());
    }

    public EntityConstructor getParameterConstructor() {
        return entityConstructor;
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

}
