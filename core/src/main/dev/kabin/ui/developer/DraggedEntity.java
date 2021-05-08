package dev.kabin.ui.developer;

import dev.kabin.entities.libgdximpl.EntityLibgdx;

/**
 * Keeps a record of an entity, its current position (x,y), and a mouse position (x,y).
 * Based on this data, we can calculate the new position of the given entity after a mouse drag.
 * The update scheme is:
 * <pre>
 *     entity pos -> entity pos + delta mouse position
 * </pre>
 */
class DraggedEntity {
    private final float entityOriginalX, entityOriginalY;
    private final float initialMouseX, initialMouseY;
    private final EntityLibgdx entity;

    public DraggedEntity(float initialMouseX, float initialMouseY, EntityLibgdx entity) {
        this.entityOriginalX = entity.getX();
        this.entityOriginalY = entity.getY();
        this.initialMouseX = initialMouseX;
        this.initialMouseY = initialMouseY;
        this.entity = entity;
    }

    public float getEntityOriginalX() {
        return entityOriginalX;
    }

    public float getEntityOriginalY() {
        return entityOriginalY;
    }

    public EntityLibgdx getEntity() {
        return entity;
    }

    public float getInitialMouseX() {
        return initialMouseX;
    }

    public float getInitialMouseY() {
        return initialMouseY;
    }
}
