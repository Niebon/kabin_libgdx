package dev.kabin.ui.developer.widgets;

import dev.kabin.entities.Entity;

/**
 * Keeps a record of an entity, its current position (x,y), and a mouse position (x,y).
 * Based on this data, we can calculate the new position of the given entity after a mouse drag.
 * The update scheme is:
 * <pre>
 *     entity pos -> entity pos + delta mouse position
 * </pre>
 */
public class DraggedEntity {
    private final float entityOriginalX, entityOriginalY;
    private final float initialMouseX, getInitialMouseY;
    private final Entity entity;

    public DraggedEntity(float originalX, float entityOriginalY, float initialMouseX, float getInitialMouseY, Entity entity) {
        this.entityOriginalX = originalX;
        this.entityOriginalY = entityOriginalY;
        this.initialMouseX = initialMouseX;
        this.getInitialMouseY = getInitialMouseY;
        this.entity = entity;
    }

    public float getEntityOriginalX() {
        return entityOriginalX;
    }

    public float getEntityOriginalY() {
        return entityOriginalY;
    }

    public Entity getEntity() {
        return entity;
    }

    public float getInitialMouseX() {
        return initialMouseX;
    }

    public float getInitialMouseY() {
        return getInitialMouseY;
    }
}
