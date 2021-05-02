package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.fp.FloatSupplier;
import dev.kabin.util.points.ImmutablePointFloat;
import dev.kabin.util.points.PointFloat;
import dev.kabin.util.shapes.RectFloat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntitySelection {

    private final Set<EntityLibgdx> currentlySelectedEntities = new HashSet<>();
    private final Supplier<MouseEventUtil> mouseEventUtil;
    private final FloatSupplier camPosX;
    private final FloatSupplier camPosY;
    private RectFloat backingRect;
    private ImmutablePointFloat begin;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final EntitySelectionMuters entitySelectionMuters = new EntitySelectionMuters();

    public EntitySelection(Supplier<MouseEventUtil> mouseEventUtil,
                           FloatSupplier camPosX,
                           FloatSupplier camPosY) {
        this.mouseEventUtil = mouseEventUtil;
        this.camPosX = camPosX;
        this.camPosY = camPosY;
    }

    public void render(Consumer<Consumer<EntityLibgdx>> forEachEntity) {

        if (entitySelectionMuters.shouldMuteDragging()) {
            return;
        }

        if (begin != null) {
            float minX = Math.min(begin.x(), mouseEventUtil.get().getXRelativeToUI());
            float minY = Math.min(begin.y(), mouseEventUtil.get().getYRelativeToUI());
            float width = Math.abs(begin.x() - mouseEventUtil.get().getXRelativeToUI());
            float height = Math.abs(begin.y() - mouseEventUtil.get().getYRelativeToUI());

            float offsetX = camPosX.get() - Gdx.graphics.getWidth() * 0.5f;
            float offsetY = camPosY.get() - Gdx.graphics.getHeight() * 0.5f;
            backingRect = new RectFloat(minX + offsetX, minY + offsetY, width, height);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 1, 1);
            shapeRenderer.rect(begin.x(), begin.y(), mouseEventUtil.get().getXRelativeToUI() - begin.x(),
                    mouseEventUtil.get().getYRelativeToUI() - begin.y());
            shapeRenderer.end();

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

    public Set<EntityLibgdx> getCurrentlySelectedEntities() {
        return Collections.unmodifiableSet(currentlySelectedEntities);
    }

    public void receiveDragListenerFrom(Window window) {
        entitySelectionMuters.add(window);
    }

    public void removeDragListenerTo(Window window) {
        entitySelectionMuters.remove(window);
    }

    private class EntitySelectionMuters {

        private final ArrayList<Window> muters = new ArrayList<>();

        private void add(Window draggable) {
            muters.add(draggable);
        }

        boolean shouldMuteDragging() {
            float x = mouseEventUtil.get().getPositionRelativeToUI().x();
            float y = mouseEventUtil.get().getPositionRelativeToUI().y();
            return muters.stream()
                    .map(w -> new RectFloat(w.getX(), w.getY(), w.getWidth(), w.getHeight()))
                    .anyMatch(r -> r.contains(x, y));
        }

        public boolean remove(Window draggable) {
            return muters.remove(draggable);
        }
    }
}
