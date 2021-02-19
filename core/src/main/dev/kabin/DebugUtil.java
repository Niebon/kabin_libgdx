package dev.kabin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.kabin.util.shapes.primitive.MutableRectInt;

public class DebugUtil {

    /**
     * Expects a square (the current camera bounds), collision predicate, and a square render procedure.
     * Given this data, we render collision for each point.
     */
    static void renderEachCollisionPoint(ShapeRenderer renderer, MutableRectInt currentCameraBounds, float scaleFactor) {
            for (int i = currentCameraBounds.getMinX(); i < currentCameraBounds.getMaxX(); i++) {
                for (int j = currentCameraBounds.getMinY(); j < currentCameraBounds.getMaxY(); j++) {
                    if (GlobalData.getWorldState().isCollisionAt(i, j)) {
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

}
