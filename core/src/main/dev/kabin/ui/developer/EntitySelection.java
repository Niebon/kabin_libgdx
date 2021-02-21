package dev.kabin.ui.developer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.entities.impl.Entity;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.points.*;
import dev.kabin.util.shapes.RectFloat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EntitySelection {

    private final Set<Entity> currentlySelectedEntities = new HashSet<>();
    private RectFloat backingRect;
    private ImmutablePointFloat begin;

    public void render() {
        if (begin != null) {
            float minX = Math.min(begin.x(), GlobalData.mouseEventUtil.getXRelativeToUI());
            float minY = Math.min(begin.y(), GlobalData.mouseEventUtil.getYRelativeToUI());
            float width = Math.abs(begin.x() - GlobalData.mouseEventUtil.getXRelativeToUI());
            float height = Math.abs(begin.y() - GlobalData.mouseEventUtil.getYRelativeToUI());

            float offsetX = MainGame.camera.getCamera().position.x - MainGame.screenWidth * 0.5f;
            float offsetY = MainGame.camera.getCamera().position.y - GlobalData.screenHeight * 0.5f;
            backingRect = new RectFloat(minX + offsetX, minY + offsetY, width, height);

            GlobalData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            GlobalData.shapeRenderer.setColor(0, 1, 1, 1);
            GlobalData.shapeRenderer.rect(begin.x(), begin.y(), GlobalData.mouseEventUtil.getXRelativeToUI() - begin.x(),
                    GlobalData.mouseEventUtil.getYRelativeToUI() - begin.y());
            GlobalData.shapeRenderer.end();

            // By abuse of the word "render" include this here...
            GlobalData.getWorldState().actionForEachEntityOrderedByType(e -> {
                if (backingRect.contains(e.getX(), e.getY())) {
                    currentlySelectedEntities.add(e);
                } else {
                    currentlySelectedEntities.remove(e);
                }
            });
        }
    }

    public void begin() {
        currentlySelectedEntities.clear();
        begin = PointFloat.immutable(GlobalData.mouseEventUtil.getXRelativeToUI(), GlobalData.mouseEventUtil.getYRelativeToUI());
    }

    // End, but only clear the selected dev.kabin.entities after the begin() call.
    public void end() {
        begin = null;
    }

    public Set<Entity> getCurrentlySelectedEntities() {
        return Collections.unmodifiableSet(currentlySelectedEntities);
    }
}
