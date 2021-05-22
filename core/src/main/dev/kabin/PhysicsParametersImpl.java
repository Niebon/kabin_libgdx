package dev.kabin;

import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.EntityPhysicsEngine;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.util.events.KeyCode;
import dev.kabin.util.events.KeyEventUtil;
import org.jetbrains.annotations.NotNull;

class PhysicsParametersImpl implements PhysicsParameters {

    private final float scale;
    @NotNull
    private final WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation;
    @NotNull
    private final KeyEventUtil keyEventUtil;

    PhysicsParametersImpl(float scale, @NotNull WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation,
                          @NotNull KeyEventUtil keyEventUtil) {
        this.scale = scale;
        this.worldRepresentation = worldRepresentation;
        this.keyEventUtil = keyEventUtil;
    }

    @Override
    public boolean isCollisionAt(int x, int y) {
        return worldRepresentation.isCollisionAt(x, y);
    }

    @Override
    public boolean isLadderAt(int x, int y) {
        return worldRepresentation.isLadderAt(x, y);
    }

    @Override
    public float getVectorFieldX(int x, int y) {
        return worldRepresentation.getVectorFieldX(x, y);
    }

    @Override
    public float getVectorFieldY(int x, int y) {
        return worldRepresentation.getVectorFieldY(x, y);
    }

    @Override
    public boolean isPressed(KeyCode keycode) {
        return keyEventUtil.isPressed(keycode);
    }

    @Override
    public float dt() {
        return EntityPhysicsEngine.DT_SECONDS;
    }

    @Override
    public float meter() {
        return EntityPhysicsEngine.METER;
    }
}
