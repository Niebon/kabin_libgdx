package dev.kabin;

import dev.kabin.utilities.Procedures;
import dev.kabin.utilities.functioninterfaces.BiIntConsumer;
import dev.kabin.utilities.functioninterfaces.BiIntPredicate;
import dev.kabin.utilities.shapes.RectInt;

public class DebugUtility {

    /**
     * Expects a square (the current camera bounds), collision predicate, and a square render procedure.
     * Given this data, we render collision for each point.
     *
     * @param currentCameraBounds
     * @param collisionPredicate
     * @param renderSquare
     */
    static void renderEachCollisionPoint(RectInt currentCameraBounds, BiIntPredicate collisionPredicate, BiIntConsumer renderSquare) {
        Procedures.forEachIntPairIn(currentCameraBounds, (i, j) -> {
            if (collisionPredicate.test(i,j)) renderSquare.accept(i,j);
        });
    }

}
