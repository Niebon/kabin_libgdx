package dev.kabin.entities.impl;

import dev.kabin.entities.impl.animation.enums.Inanimate;

public class EntityInanimate extends AbstractLibgdxEntity<Inanimate> {

    EntityInanimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityType getType() {
        return EntityType.ENTITY_INANIMATE;
    }

}
