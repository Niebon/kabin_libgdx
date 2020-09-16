package dev.kabin.entities;

import dev.kabin.utilities.GameData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Player extends EntitySimple {


    public Player(@NotNull JSONObject o) {
        this(o.getFloat("x") * GameData.scaleFactor, o.getFloat("y") * GameData.scaleFactor,
                o.getString("imageResource"), GameData.scaleFactor);
    }

    public Player(float x, float y, String imageResource, float scale) {
        super(x, y, imageResource, scale);
    }
}
