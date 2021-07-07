package dev.kabin.entities.libgdximpl;

import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.enums.Tile;
import dev.kabin.util.Functions;
import dev.kabin.util.geometry.points.ImmutablePointInt;
import dev.kabin.util.geometry.points.PointInt;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CollisionTile extends CollisionEntity {

	// Constants
	public static final String FRAME_INDEX = "frame_index";
	public static final String TILE = "tile";
	public static final int TILE_SIZE = 16;

	// Static variables
	private static final Map<ImmutablePointInt, CollisionTile> objectPool = new ConcurrentHashMap<>();

	// Fields
	private final Tile tile;
	private final int index;

	// Variables
	private int unscaledX;
	private int unscaledY;

	/**
	 * This constructor is removes any preexisting {@link CollisionTile} at the location specified by {@link #getXAsInt()},
	 * {@link #getYAsInt()}.
	 *
	 * @param parameters constructor parameters.
	 */
	public CollisionTile(EntityParameters parameters) {
		super(parameters);
		tile = parameters.getMaybe(TILE, Tile.class).orElseThrow();
		final Optional<AbstractAnimationPlaybackLibgdx<Tile>> animationPlaybackImpl = Optional.ofNullable(getAnimationPlaybackImpl(Tile.class));
		animationPlaybackImpl.ifPresent(a -> a.setCurrentAnimation(tile));
		index = animationPlaybackImpl
				.map(AbstractAnimationPlaybackLibgdx::getCurrentAnimationLength)
				.map(i -> Math.floorMod(parameters.<Integer>getMaybe(FRAME_INDEX).orElseThrow(), i))
				.orElse(0);
		if (objectPool.containsKey(PointInt.immutable(unscaledX, unscaledY))) {
			throw new IllegalArgumentException(("The position at which this collision tile was placed was already occupied. " +
					"Use the clearAt method to clear. Here are the coordinates (%s,%s)").formatted(getXAsInt(), getYAsInt()));
		}
		objectPool.put(PointInt.immutable(unscaledX, unscaledY), this);
		animationPlaybackImpl.ifPresent(a -> a.setSmoothParameter(1));
	}

	/**
	 * Clears any {@link CollisionTile} with unscaled coordinates x,y.
	 *
	 * @param x horizontal coordinate.
	 * @param y vertical coordinate.
	 * @return if the position was occupied, clears it and returns the occupant.
	 */
	public static Optional<CollisionTile> clearAt(int x, int y) {
		return Optional.ofNullable(objectPool.remove(PointInt.immutable(x, y)));
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void updateGraphics(GraphicsParametersLibgdx params) {
		final AbstractAnimationPlaybackLibgdx<Tile> animationPlaybackImpl = getAnimationPlaybackImpl(Tile.class);
		if (animationPlaybackImpl != null) {
			animationPlaybackImpl.setPos(x() - artMassCenterX(), y() - (artMassCenterY() - 1));
			animationPlaybackImpl.setCurrentAnimation(tile);
			animationPlaybackImpl.setShaderProgram(params.shaderFor(getGroupType()));
			animationPlaybackImpl.renderFrameByIndex(params, index);
			animationPlaybackImpl.setRenderScale(1.01f);
			actor().setBounds(
					x() * params.scaleX(),
					y() * params.scaleY(),
					animationPlaybackImpl.getWidth() * params.scaleX(),
					animationPlaybackImpl.getHeight() * params.scaleY()
			);
		}
	}


	@Override
	public void setX(float x) {
		unscaledX = Functions.snapToGrid(x, TILE_SIZE);
		super.setX(unscaledX);
	}

	@Override
	public int getXAsInt() {
		return unscaledX;
	}

	@Override
	public void setY(float y) {
		unscaledY = Functions.snapToGrid(y, TILE_SIZE);
		super.setY(unscaledY);
	}

	@Override
	public int getYAsInt() {
		return unscaledY;
	}

	@Override
	public JSONObject toJSONObject() {
		return super.toJSONObject()
				.put(FRAME_INDEX, index)
				.put(TILE, tile.name());
	}


	@Override
	public int getRootXAsInt() {
		return getXAsInt() - TILE_SIZE / 2;
	}

	@Override
	public int getRootYAsInt() {
		return getYAsInt() - TILE_SIZE / 2;
	}

	@Override
	public float artMassCenterX() {
		return 0.5f * TILE_SIZE;
	}

	@Override
	public float artMassCenterY() {
		return 0.5f * TILE_SIZE;
	}


}
