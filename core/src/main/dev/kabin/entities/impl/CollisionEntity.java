package dev.kabin.entities.impl;

import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.CollisionPool;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollisionEntity  extends EntitySimple implements CollisionData {

	CollisionEntity(EntityParameters parameters) {
        super(parameters);
    }

    @Override
    public @NotNull List<PointInt> getCollisionProfile() {
        return CollisionPool.findCollisionProfile(
        		animationPlaybackImpl.getCurrentImageAssetPath(),
                animationPlaybackImpl.getCurrentImageAssetIndex()
		);
    }

    @Override
    public @NotNull List<PointInt> getSurfaceContourProfile() {
		return CollisionPool.findSurfaceContourProfile(
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
