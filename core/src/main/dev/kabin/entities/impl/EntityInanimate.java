package dev.kabin.entities.impl;

public class EntityInanimate extends EntitySimple {

    EntityInanimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_INANIMATE;
    }

}
