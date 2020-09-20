package dev.kabin.geometry.points;

/**
 * An immutable implementation of {@link PrimitivePointInt}.
 */
public class ImmutablePointInt implements PrimitivePointInt{

	final int x, y;

	public ImmutablePointInt(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}
}
