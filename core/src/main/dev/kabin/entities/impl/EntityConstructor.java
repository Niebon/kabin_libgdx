package dev.kabin.entities.impl;

@FunctionalInterface
public interface EntityConstructor {
    EntityLibgdx construct(EntityParameters parameters);
}
