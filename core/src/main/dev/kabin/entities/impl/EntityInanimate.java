package dev.kabin.entities.impl;

public class EntityInanimate extends AbstractEntity {

    EntityInanimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_INANIMATE;
    }

}
