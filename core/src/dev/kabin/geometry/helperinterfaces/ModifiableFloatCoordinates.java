package dev.kabin.geometry.helperinterfaces;

public interface ModifiableFloatCoordinates {

    float getX();

    void setX(float x);

    float getY();

    void setY(float y);

    default void setPos(float x, float y) {
        setX(x);
        setY(y);
    }

}
