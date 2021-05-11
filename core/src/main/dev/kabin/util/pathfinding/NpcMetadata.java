package dev.kabin.util.pathfinding;

/**
 * Npc metadata. This class handles message passing from Npc implementations
 * to path finding for Npcs.
 */
public record NpcMetadata(
		float x,
		float y,
		float vx,
		float vy,
		float heightConstant
) {

}
