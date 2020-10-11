package dev.kabin.entities;

import java.util.Optional;

public class Player extends EntitySimple {


    static Player instance;

    Player(EntityParameters parameters) {
        super(parameters);
    }

    public static Optional<Player> getInstance() {
        return Optional.ofNullable(instance);
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.PLAYER;
    }

    public void freeze() {

    }

    public void triggerFlashLight() {
    }

    public void toggleRunSpeed() {
    }

    public void toggleWalkSpeed() {
    }

    public void interactWithNearestIntractable() {
    }

    public Optional<Object> getHeldEntity() {
        return Optional.empty();
    }

    public void releaseHeldEntity() {
    }

    public void throwHeldEntity() {
    }
}
