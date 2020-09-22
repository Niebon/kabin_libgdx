package dev.kabin.entities;

import dev.kabin.geometry.points.PrimitivePointInt;
import dev.kabin.utilities.pools.CollisionPool;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

public class CollisionEntity extends EntitySimple implements CollisionData {

	public CollisionEntity(JSONObject jsonObject) {
		super(jsonObject);
	}

	public CollisionEntity(float x, float y, String atlasPath, float scale) {
		super(x, y, atlasPath, scale);
	}

	@Override
	public @NotNull List<PrimitivePointInt> getCollisionProfile() {
		return CollisionPool.findCollisionProfile(animationBundle.getCurrentImageAssetPath(),
				animationBundle.getCurrentImageAssetIndex());
	}

	@Override
	public @NotNull List<PrimitivePointInt> getSurfaceContourProfile() {
		return CollisionPool.findSurfaceContourProfile(animationBundle.getCurrentImageAssetPath(),
				animationBundle.getCurrentImageAssetIndex());
	}

	@Override
	public int getRootX() {
		return 0;
	}

	@Override
	public int getRootY() {
		return 0;
	}

	@Override
	public ImageAnalysisPool.Analysis getPixelAnalysis() {
		return ImageAnalysisPool.findAnalysis(animationBundle.getCurrentImageAssetPath(),
				animationBundle.getCurrentImageAssetIndex());
	}

	@Override
	public EntityFactory.EntityType getType() {
		return EntityFactory.EntityType.COLLISION_ENTITY;
	}
}
