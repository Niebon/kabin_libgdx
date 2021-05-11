package dev.kabin.util.pathfinding;

/**
 * An object for passing physics constant to path finding objects.
 */
public record PhysicsConstants(
		float g, // gravity constant
		float meter // meter SI unit
) {

}
