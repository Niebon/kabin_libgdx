package dev.kabin.entities;

import dev.kabin.components.Component;
import dev.kabin.geometry.points.Point;
import dev.kabin.geometry.points.PointInt;
import dev.kabin.geometry.points.PrimitivePointInt;
import dev.kabin.utilities.functioninterfaces.PrimitiveIntPairConsumer;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;


public interface CollisionData extends ImageAnalysisPool.Analysis.Analyzable {

    default void initCollisionData() {
        //initCollisionData(GameData.getRootComponent());
    }

    default void removeCollisionData() {
        //removeCollisionData(GameData.getRootComponent());
    }

    @NotNull
    List<PrimitivePointInt> getCollisionProfile();

    @NotNull
    List<PrimitivePointInt> getSurfaceContourProfile();

    @NotNull
    default Stream<PointInt> getSurfaceContourRelativeToOrigin() {
        final int rootX = getRootX(), rootY = getRootY();
        return getSurfaceContourProfile().stream()
                // Using p.x, p.y returns unboxed primitives.
                .map(p -> Point.of(p.getX() + rootX, p.getY() + rootY));
    }

    default void initCollisionData(final Component component) {
        actionEachCollisionPoint(component::increaseCollisionAt);
    }

    default void actionEachCollisionPoint(PrimitiveIntPairConsumer action) {
        final int rootX = getRootX(), rootY = getRootY();
        final List<PrimitivePointInt> profile = getCollisionProfile();

        if (angleRad() == 0) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = profile.size(); i < n; i++) {
                action.action(profile.get(i).getX() + rootX, profile.get(i).getY() + rootY);
            }
        } else {
            // TODO: Test the below code.
            double angleRad = angleRad();
            double cs = Math.cos(angleRad);
            double sn = Math.sin(angleRad);
            int pixelMassCenterX = getPixelAnalysis().getPixelMassCenterXInt();
            int pixelMassCenterY = getPixelAnalysis().getPixelMassCenterYInt();

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = profile.size(); i < n; i++) {


                int x = profile.get(i).getX();
                int y = profile.get(i).getY();

                int xRelPixelMc = x - pixelMassCenterX;
                int yRelPixelMc = y - pixelMassCenterY;

                int xRotRelOrigin = (int) Math.round(rootX + cs * xRelPixelMc - sn * yRelPixelMc);
                int yRotRelOrigin = (int) Math.round(rootY + sn * xRelPixelMc + sn * yRelPixelMc);

                action.action(xRotRelOrigin, yRotRelOrigin);
            }
        }
    }

    default double angleRad() {
        return 0;
    }

//    default void removeCollisionData(final Component component) {
//        actionEachCollisionPoint(component::decreaseCollisionAt);
//    }

    int getRootX();

    int getRootY();

}
