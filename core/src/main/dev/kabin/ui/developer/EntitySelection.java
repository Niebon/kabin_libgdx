package dev.kabin.ui.developer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.entities.impl.Entity;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.functioninterfaces.FloatSupplier;
import dev.kabin.util.points.ImmutablePointFloat;
import dev.kabin.util.points.PointFloat;
import dev.kabin.util.shapes.RectFloat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntitySelection {

    private final Set<Entity> currentlySelectedEntities = new HashSet<>();
    private final Supplier<MouseEventUtil> mouseEventUtil;
    private final FloatSupplier camPosX;
    private final FloatSupplier camPosY;
    private RectFloat backingRect;
    private ImmutablePointFloat begin;

    public EntitySelection(Supplier<MouseEventUtil> mouseEventUtil,
                           FloatSupplier camPosX,
                           FloatSupplier camPosY) {
        this.mouseEventUtil = mouseEventUtil;
        this.camPosX = camPosX;
        this.camPosY = camPosY;
    }

    public void render(Consumer<Consumer<Entity>> forEachEntity) {
        if (begin != null) {
            float minX = Math.min(begin.x(), mouseEventUtil.get().getXRelativeToUI());
            float minY = Math.min(begin.y(), mouseEventUtil.get().getYRelativeToUI());
            float width = Math.abs(begin.x() - mouseEventUtil.get().getXRelativeToUI());
            float height = Math.abs(begin.y() - mouseEventUtil.get().getYRelativeToUI());

            float offsetX = camPosX.get() - MainGame.screenWidth * 0.5f;
            float offsetY = camPosY.get() - MainGame.screenHeight * 0.5f;
            backingRect = new RectFloat(minX + offsetX, minY + offsetY, width, height);

            GlobalData.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            GlobalData.shapeRenderer.setColor(0, 1, 1, 1);
            GlobalData.shapeRenderer.rect(begin.x(), begin.y(), mouseEventUtil.get().getXRelativeToUI() - begin.x(),
                    mouseEventUtil.get().getYRelativeToUI() - begin.y());
            GlobalData.shapeRenderer.end();

            // By abuse of the word "render" include this here...
            forEachEntity.accept(e -> {
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
        begin = PointFloat.immutable(
                mouseEventUtil.get().getXRelativeToUI(),
                mouseEventUtil.get().getYRelativeToUI()
        );
    }

    // End, but only clear the selected dev.kabin.entities after the begin() call.
    public void end() {
        begin = null;
    }

    public Set<Entity> getCurrentlySelectedEntities() {
        return Collections.unmodifiableSet(currentlySelectedEntities);
    }
}
