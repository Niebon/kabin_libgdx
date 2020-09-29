package dev.kabin.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.graphics.animation.AnimationBundle;
import dev.kabin.graphics.animation.AnimationBundleFactory;
import dev.kabin.utilities.GameData;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.json.JSONObject;

public class EntitySimple implements Entity {

    protected final AnimationBundle animationBundle;
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
        this.animationBundle = AnimationBundleFactory.loadFromAtlasPath(atlasPath);
    }

    @Override
    public void render(SpriteBatch batch, float stateTime) {
        animationBundle.setX(x);
        animationBundle.setY(y);
        animationBundle.setScale(scale);
        animationBundle.renderNextAnimationFrame(batch, stateTime);
    }

    @Override
    public void updatePhysics() {
        // Update x and y accordingly.
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
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_SIMPLE;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public ImageAnalysisPool.Analysis getPixelAnalysis() {
        return ImageAnalysisPool.findAnalysis(animationBundle.getCurrentImageAssetPath(), animationBundle.getCurrentImageAssetIndex());
    }
}
