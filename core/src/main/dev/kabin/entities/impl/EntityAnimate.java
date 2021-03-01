package dev.kabin.entities.impl;

public class EntityAnimate extends AbstractEntity {

    EntityAnimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_ANIMATE;
    }

}
