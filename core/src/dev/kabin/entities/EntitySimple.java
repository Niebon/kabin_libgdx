package dev.kabin.entities;

import dev.kabin.utilities.GameData;
import org.json.JSONObject;

public class EntitySimple implements Entity {

    private final String atlasPath;
    private float x, y, scale;

    public EntitySimple(JSONObject jsonObject) {
        this(jsonObject.getFloat("x") * GameData.scaleFactor, jsonObject.getFloat("y") * GameData.scaleFactor,
                jsonObject.getString("atlasPath"), GameData.scaleFactor);
    }

    public EntitySimple(float x, float y, String atlasPath, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.atlasPath = atlasPath;


    }

    @Override
    public void render() {

    }

    @Override
    public void updatePhysics() {

    }

    @Override
    public int getLayer() {
        return 0;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String getAtlasPath() {
        return atlasPath;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }
}
