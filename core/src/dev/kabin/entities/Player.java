package dev.kabin.entities;

import dev.kabin.utilities.GameData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Optional;

public class Player extends EntitySimple {


    static Player instance;


    public Player(@NotNull JSONObject o) {
        this(o.getFloat("x") * GameData.scaleFactor, o.getFloat("y") * GameData.scaleFactor,
                o.getString("imageResource"), GameData.scaleFactor);
    }

    public static Optional<Player> getInstance() {
        return Optional.ofNullable(instance);
    }

    public Player(float x, float y, String imageResource, float scale) {
        super(x, y, imageResource, scale);
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

    public void interactWithNearestInteractable() {
    }

    public Optional<Object> getHeldEntity() {
        return null;
    }

    public void releaseHeldEntity() {
    }

    public void throwHeldEntity() {
    }
}
