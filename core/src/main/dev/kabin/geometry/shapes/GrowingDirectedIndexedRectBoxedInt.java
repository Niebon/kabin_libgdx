package dev.kabin.geometry.shapes;

import dev.kabin.geometry.points.PointInt;
import org.jetbrains.annotations.NotNull;

public class GrowingDirectedIndexedRectBoxedInt extends GrowingDirectedIndexedRectBoxed<Integer> {

    public GrowingDirectedIndexedRectBoxedInt(@NotNull PointInt pointInt) {
        this(pointInt.x, pointInt.y, 0, 0);
    }

    public GrowingDirectedIndexedRectBoxedInt(int x, int y, int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        setMinX(x);
        setMaxX(x + width);
        setMinY(y);
        setMaxY(y + height);
    }

    public GrowingDirectedIndexedRectBoxedInt(GrowingDirectedIndexedRectBoxed<Integer> other) {
        super(other);
    }

    @NotNull
    @Override
    public Integer getWidth() {
        return getMaxX() - getMinX();
    }

    @NotNull
    @Override
    public Integer getHeight() {
        return getMaxY() - getMinY();
    }

}
