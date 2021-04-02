package dev.kabin.entities.impl;

import dev.kabin.entities.impl.animation.enums.Animate;

public class EntityAnimate extends AbstractLibgdxEntity<Animate> {

    EntityAnimate(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public EntityType getType() {
        return EntityType.ENTITY_ANIMATE;
    }

}
