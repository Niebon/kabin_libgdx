package dev.kabin.entities;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EntityFactory {

    // Enum class carrying all entity types
    public enum EntityType {
        //BEAR(Bear::new, Bear::new, EntityGroupProvider.Type.FOCAL_POINT),
        //CAT(Cat::new, Cat::new, EntityGroupProvider.Type.FOCAL_POINT),
        //COLLISION_ENTITY(CollisionEntity::new, CollisionEntity::new, EntityGroupProvider.Type.FOCAL_POINT),
        //COLLISION_ENTITY_MOVABLE(CollisionEntityMovable::new, CollisionEntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
        //COLLISION_ENTITY_THROWABLE(CollisionEntityThrowable::new, CollisionEntityThrowable::new, EntityGroupProvider.Type.FOCAL_POINT),
        //ENTITY_BACKGROUND(EntityBackground::new, EntityBackground::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND),
        //ENTITY_BACKGROUND_LAYER_2(EntityBackgroundLayer2::new, EntityBackgroundLayer2::newFromMouseClick, EntityGroupProvider.Type.BACKGROUND_LAYER_2),
        //ENTITY_FOREGROUND(EntityForeground::new, EntityForeground::newFromMouseClick, EntityGroupProvider.Type.FOREGROUND),
        //ENTITY_MOVABLE(EntityMovable::new, EntityMovable::new, EntityGroupProvider.Type.FOCAL_POINT),
        //ENTITY_LUMINATING(EntityLuminating::new, EntityLuminating::new, EntityGroupProvider.Type.FOCAL_POINT),
        ENTITY_SIMPLE(EntitySimple::new, EntitySimple::new, EntityGroupProvider.Type.FOCAL_POINT),
        //ENTITY_THROWABLE(EntityThrowable::new, EntityThrowable::new, EntityGroupProvider.Type.FOCAL_POINT),
        //FOX(Fox::new, Fox::new, EntityGroupProvider.Type.FOCAL_POINT),
        //GROUND(Ground::new, Ground::new, null),
        //GROUND_TILE(GroundTile::new, null, EntityGroupProvider.Type.GROUND),
        //LADDER(Ladder::new, Ladder::new, EntityGroupProvider.Type.FOCAL_POINT),
        //MAP_CONNECTOR(MapConnector::new, MapConnector::new, null),
        //OON(Moon::new, Moon::new, EntityGroupProvider.Type.SKY),
        PLAYER(Player::new, Player::new, EntityGroupProvider.Type.FOCAL_POINT);
        //STARS(Stars::new, Stars::new, EntityGroupProvider.Type.SKY),
        //STATIC_BACKGROUND(StaticBackground::new, null, EntityGroupProvider.Type.STATIC_BACKGROUND),
        //SKY(Sky::new, null, EntityGroupProvider.Type.SKY),
        //SHORTCUT(Shortcut::new, Shortcut::new, EntityGroupProvider.Type.FOCAL_POINT);

        final JsonConstructor jsonConstructor;
        final MouseClickConstructor mouseClickConstructor;
        final EntityGroupProvider.Type groupType;

        EntityType(JsonConstructor jsonConstructor,
                   // This constructor can be redundant; see GroundTile.
                   @Nullable MouseClickConstructor mouseClickConstructor,
                   EntityGroupProvider.Type groupType) {
            this.jsonConstructor = jsonConstructor;
            this.mouseClickConstructor = mouseClickConstructor;
            this.groupType = groupType;
        }

        public JsonConstructor getJsonConstructor() {
            return jsonConstructor;
        }

        public MouseClickConstructor getMouseClickConstructor() {
            return mouseClickConstructor;
        }

        public EntityGroupProvider.Type groupType() {
            return groupType;
        }
    }

    @FunctionalInterface
    public interface JsonConstructor {
        Entity construct(JSONObject jsonObject);
    }

    @FunctionalInterface
    public interface MouseClickConstructor {
        Entity construct(float mouseClickX, float mouseClickY, String imageResource, float scale);
    }

}
