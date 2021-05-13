package dev.kabin.util.pathfinding;

/**
 * A being metadata. This class handles message passing from Npc implementations
 * to path finding for beings.
 */
public record BeingMetadata(
		float x,
		float y,
		float movementSpeed,
		float jumpSpeed,
		float heightConstant
) {

}
