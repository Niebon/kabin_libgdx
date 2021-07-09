package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.util.geometry.points.PointInt;
import dev.kabin.util.geometry.primitive.ImmutableRectInt;
import dev.kabin.util.geometry.primitive.RectInt;
import dev.kabin.util.pools.CollisionPool;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CollisionEntity extends AbstractLibgdxEntity implements CollisionData {

    private final TextureAtlas atlas;

    CollisionEntity(EntityParameters parameters) {
        super(parameters);
        atlas = parameters.textureAtlas();
    }

    @Override
    public @NotNull List<PointInt> getCollisionProfile() {
        final AbstractAnimationPlaybackLibgdx<?> animationPlaybackImpl = getAnimationPlaybackImpl();
        if (animationPlaybackImpl == null) return Collections.emptyList();
        return CollisionPool.findCollisionProfile(
                this::findRegion,
                animationPlaybackImpl.getCurrentImageAssetPath(),
                animationPlaybackImpl.getCurrentImageAssetIndex()
        );
    }

    @NotNull
    private RectInt findRegion(String path, Integer index) {
        var atlasRegion = atlas.findRegion(path, index);
        int x = atlasRegion.getRegionX();
        int y = atlasRegion.getRegionY();
        int width = atlasRegion.getRegionWidth();
        int height = atlasRegion.getRegionHeight();
        return new ImmutableRectInt(x, y, width, height);
    }

    @Override
    public @NotNull List<PointInt> getSurfaceContourProfile() {
        final AbstractAnimationPlaybackLibgdx<?> animationPlaybackImpl = getAnimationPlaybackImpl();
        if (animationPlaybackImpl == null) {
            return Collections.emptyList();
        }
        return CollisionPool.findSurfaceContourProfile(
                this::findRegion,
                animationPlaybackImpl.getCurrentImageAssetPath(),
                animationPlaybackImpl.getCurrentImageAssetIndex()
        );
    }

    @Override
    public int getRootXAsInt() {
        return super.getRootXAsInt();
    }

    @Override
    public int getRootYAsInt() {
        return super.getRootYAsInt();
    }

}
