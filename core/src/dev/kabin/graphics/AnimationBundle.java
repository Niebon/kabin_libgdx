package dev.kabin.graphics;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Direction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationBundle implements Animations, Disposable {

    private static final float DURATION_SECONDS = 0.1f;

    private final SpriteBatch batch;
    private final Map<AnimationType, Animation<TextureRegion>> animations;
    private final Array<TextureAtlas.AtlasRegion> regions;
    float x, y, width, height, scale;
    private AnimationType currentAnimationType = AnimationType.DEFAULT_RIGHT;

    public AnimationBundle(
            String atlasRegionPath,
            Map<AnimationType, List<Integer>> animations
    ) {
        this.batch = new SpriteBatch();
        this.regions = GlobalData.atlas.findRegions(atlasRegionPath);
        this.animations = animations.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    List<Integer> indices = e.getValue();
                    Array<TextureRegion> textureArray = new Array<>(indices.size());
                    for (int i : indices) {
                        textureArray.add(regions.get(i));
                    }
                    return new Animation<>(DURATION_SECONDS, textureArray);
                }
        ));
    }


    @Override
    public void setCurrentAnimation(AnimationType animationType) {
        currentAnimationType = animationType;
    }

    @Override
    public void renderNextAnimationFrame(float stateTime) {
        if (!animations.containsKey(currentAnimationType)) return;

        final TextureRegion region;

        if (currentAnimationType.isLooping()) {
            region = animations.get(currentAnimationType).getKeyFrame(stateTime, true);
        } else {
            region = animations.get(currentAnimationType).getKeyFrame(stateTime);
        }
        // Switch to default if last frame is not repeating
        if (!currentAnimationType.isLastFrameRepeating() &&
                !currentAnimationType.isLooping() &&
                animations.get(currentAnimationType).isAnimationFinished(stateTime)) {
            currentAnimationType = currentAnimationType.getDirection() == Direction.RIGHT ? AnimationType.DEFAULT_RIGHT : AnimationType.DEFAULT_LEFT;
        }
        batch.begin();
        batch.draw(region, getX() * getScale(), getY() * getScale(), getWidth() * getScale(), getHeight() * getScale());
        batch.end();
    }

    @Override
    public void renderFrameByIndex(int index) {
        batch.begin();
        batch.draw(regions.get(index),
                getX() * getScale(),
                getY() * getScale(),
                getWidth() * getScale(),
                getHeight() * getScale()
        );
        batch.end();
    }


    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void setHeight(float height) {
        this.height = height;
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
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
