package dev.kabin.entities.libgdximpl;

@FunctionalInterface
public interface EntityConstructor {
    EntityLibgdx construct(EntityParameters parameters);
}
