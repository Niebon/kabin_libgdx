package dev.kabin.util.shapes;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RectBoxedInt extends AbstractRectBoxed<Integer> {

    public RectBoxedInt(int x, int y, int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException();
        setMinX(x);
        setMaxX(x + width);
        setMinY(y);
        setMaxY(y + height);
    }

    @Contract("_,_->this")
    public RectBoxedInt translate(int dx, int dy) {
        setMinX(getMinX() + dx);
        setMaxX(getMaxX() + dx);
        setMinY(getMinY() + dy);
        setMaxY(getMaxY() + dy);
        return this;
    }

    @Contract("_->this")
    public RectBoxedInt scale(double scale) {
        setMinX((int) Math.round(getMinX() * scale));
        setMaxX((int) Math.round(getMaxX() * scale));
        setMinY((int) Math.round(getMinY() * scale));
        setMaxY((int) Math.round(getMaxY() * scale));
        return this;
    }

    @Override
    public double getCenterX() {
        return 0.5 * (getMaxX().doubleValue() + getMinX().doubleValue());
    }

    @Override
    public double getCenterY() {
        return 0.5 * (getMaxY().doubleValue() + getMinY().doubleValue());
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
