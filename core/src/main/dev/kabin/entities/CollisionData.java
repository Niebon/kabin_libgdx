package dev.kabin.entities;

import dev.kabin.GlobalData;
import dev.kabin.components.Component;
import dev.kabin.utilities.functioninterfaces.PrimitiveIntPairConsumer;
import dev.kabin.utilities.points.ModifiablePointInt;
import dev.kabin.utilities.points.Point;
import dev.kabin.utilities.points.PointInt;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;


public interface CollisionData extends ImageAnalysisPool.Analysis.Analyzable {

    default void initCollisionData() {
        if (GlobalData.getRootComponent() != null)
            initCollisionData(GlobalData.getRootComponent());
    }

    default void removeCollisionData() {
        if (GlobalData.getRootComponent() != null)
            removeCollisionData(GlobalData.getRootComponent());
    }

    @NotNull
    List<PointInt> getCollisionProfile();

    @NotNull
    List<PointInt> getSurfaceContourProfile();

    @NotNull
    default Stream<ModifiablePointInt> getSurfaceContourRelativeToOrigin() {
        final int rootX = getRootX(), rootY = getRootY();
        return getSurfaceContourProfile().stream()
                .map(p -> Point.of(p.getX() + rootX, p.getY() + rootY));
    }

    default void initCollisionData(final Component component) {
        actionEachCollisionPoint(component::incrementCollisionAt);
    }

    default void actionEachCollisionPoint(PrimitiveIntPairConsumer consumer) {
        final int rootX = getRootX(), rootY = getRootY();
        final List<PointInt> profile = getCollisionProfile();

        if (angleRad() == 0) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = profile.size(); i < n; i++) {
                consumer.accept(profile.get(i).getX() + rootX, profile.get(i).getY() + rootY);
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

                consumer.accept(xRotRelOrigin, yRotRelOrigin);
            }
        }
    }

    /**
     * The current orientation.
     *
     * @return
     */
    default double angleRad() {
        return 0;
    }

    default void removeCollisionData(final Component component) {
        actionEachCollisionPoint(component::decrementCollisionAt);
    }

    int getRootX();

    int getRootY();

}
