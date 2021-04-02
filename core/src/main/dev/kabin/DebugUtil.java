package dev.kabin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.kabin.entities.Entity;
import dev.kabin.util.functioninterfaces.BiIntPredicate;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DebugUtil {

    /**
     * Expects a square (the current camera bounds), collision predicate, and a square render procedure.
     * Given this data, we render collision for each point.
     */
    static void renderEachCollisionPoint(
            @NotNull BiIntPredicate collisionPredicate,
            @NotNull ShapeRenderer renderer,
            @NotNull MutableRectInt currentCameraBounds,
            float scaleFactor) {
        for (int i = currentCameraBounds.getMinX(); i < currentCameraBounds.getMaxX(); i++) {
            for (int j = currentCameraBounds.getMinY(); j < currentCameraBounds.getMaxY(); j++) {
                if (collisionPredicate.test(i, j)) {
                    renderer.begin(ShapeRenderer.ShapeType.Filled);
                    renderer.setColor(Color.RED);
                    float x = (i - currentCameraBounds.getMinX()) * scaleFactor;
                    float y = (j - currentCameraBounds.getMinY()) * scaleFactor;
                    renderer.rect(x, y, scaleFactor, scaleFactor);
                    renderer.end();
                }
            }
        }
    }

    /**
     * Draws root of each entity.
     */
    static void renderEachRoot(Consumer<Consumer<Entity<?, ?, ?>>> forEachEntity,
                               ShapeRenderer renderer,
                               MutableRectInt currentCameraBounds,
                               float scaleFactor) {
        forEachEntity.accept(e -> {
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(Color.GREEN);
            float x = e.getX() - currentCameraBounds.getMinX() * scaleFactor;
            float y = e.getY() - currentCameraBounds.getMinY() * scaleFactor;
            renderer.rect(x, y, scaleFactor, scaleFactor);
            renderer.end();
        });
    }

}
