package dev.kabin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.kabin.utilities.shapes.primitive.MutableRectInt;

import static dev.kabin.GlobalData.shapeRenderer;

public class DebugUtil {

    /**
     * Expects a square (the current camera bounds), collision predicate, and a square render procedure.
     * Given this data, we render collision for each point.
     */
    static void renderEachCollisionPoint(MutableRectInt currentCameraBounds, float scaleFactor) {
            for (int i = currentCameraBounds.getMinX(); i < currentCameraBounds.getMaxX(); i++) {
                for (int j = currentCameraBounds.getMinY(); j < currentCameraBounds.getMaxY(); j++) {
                    if (GlobalData.getWorldState().isCollisionAt(i, j)) {
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                        shapeRenderer.setColor(Color.RED);
                        float x = (i - currentCameraBounds.getMinX()) * scaleFactor;
                        float y = (j - currentCameraBounds.getMinY()) * scaleFactor;
                        shapeRenderer.rect(x, y, scaleFactor, scaleFactor);
                        shapeRenderer.end();
                    }
                }
            }
    }

}
