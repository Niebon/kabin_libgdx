package dev.kabin.entities.impl;

import dev.kabin.entities.animation.enums.Animate;

public class EntityAnimate extends AbstractEntity<Animate> {

    EntityAnimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_ANIMATE;
    }

}
