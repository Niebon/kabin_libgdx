package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.CollisionPool;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CollisionEntity extends AbstractLibgdxEntity implements CollisionData {

	private final TextureAtlas atlas;

	CollisionEntity(EntityParameters parameters) {
		super(parameters);
		atlas = parameters.getTextureAtlas();
	}

	@Override
	public @NotNull List<PointInt> getCollisionProfile() {
		final AbstractAnimationPlaybackLibgdx<?> animationPlaybackImpl = getAnimationPlaybackImpl();
		if (animationPlaybackImpl == null) return Collections.emptyList();
		return CollisionPool.findCollisionProfile(
				atlas,
				animationPlaybackImpl.getCurrentImageAssetPath(),
				animationPlaybackImpl.getCurrentImageAssetIndex()
		);
	}

	@Override
	public @NotNull List<PointInt> getSurfaceContourProfile() {
		final AbstractAnimationPlaybackLibgdx<?> animationPlaybackImpl = getAnimationPlaybackImpl();
		if (animationPlaybackImpl == null) {
			return Collections.emptyList();
		}
		return CollisionPool.findSurfaceContourProfile(
				atlas,
				animationPlaybackImpl.getCurrentImageAssetPath(),
				animationPlaybackImpl.getCurrentImageAssetIndex()
		);
	}

	@Override
	public int getRootIntX() {
		return super.getRootIntX();
	}

	@Override
	public int getRootIntY() {
		return super.getRootIntY();
	}

}
