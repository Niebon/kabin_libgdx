package dev.kabin.util.pathfinding;

import dev.kabin.util.Direction;

/**
 * A check point in a path.
 */
record CheckPoint(int pathIndex, Direction direction, boolean jump) {
}
