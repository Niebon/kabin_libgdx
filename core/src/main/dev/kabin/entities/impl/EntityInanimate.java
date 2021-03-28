package dev.kabin.entities.impl;

import dev.kabin.entities.animation.enums.Inanimate;

public class EntityInanimate extends AbstractEntity<Inanimate> {

    EntityInanimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_INANIMATE;
    }

}
