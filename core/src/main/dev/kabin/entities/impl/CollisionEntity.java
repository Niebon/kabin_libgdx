package dev.kabin.entities.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.kabin.entities.animation.AbstractAnimationPlayback;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.CollisionPool;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class CollisionEntity<T extends Enum<T>> extends AbstractEntity<T> implements CollisionData {

	private final TextureAtlas atlas;

	CollisionEntity(EntityParameters parameters) {
		super(parameters);
		atlas = parameters.getTextureAtlas();
	}

	@Override
	public @NotNull List<PointInt> getCollisionProfile() {
		final AbstractAnimationPlayback<?> animationPlaybackImpl = getAnimationPlaybackImpl();
		if (animationPlaybackImpl == null) return Collections.emptyList();
		return CollisionPool.findCollisionProfile(
				atlas,
				animationPlaybackImpl.getCurrentImageAssetPath(),
				animationPlaybackImpl.getCurrentImageAssetIndex()
		);
	}

	@Override
	public @NotNull List<PointInt> getSurfaceContourProfile() {
		final AbstractAnimationPlayback<?> animationPlaybackImpl = getAnimationPlaybackImpl();
		if (animationPlaybackImpl == null) return Collections.emptyList();
		return CollisionPool.findSurfaceContourProfile(
				atlas,
				animationPlaybackImpl.getCurrentImageAssetPath(),
				animationPlaybackImpl.getCurrentImageAssetIndex()
		);
	}

	@Override
	public EntityFactory.EntityType getType() {
		return EntityFactory.EntityType.COLLISION_ENTITY;
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
