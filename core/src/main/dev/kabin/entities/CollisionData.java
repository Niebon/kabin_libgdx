package dev.kabin.entities;

import dev.kabin.util.functioninterfaces.PrimitiveIntPairConsumer;
import dev.kabin.util.points.ModifiablePointInt;
import dev.kabin.util.points.PointOld;
import dev.kabin.util.points.PointInt;
import dev.kabin.util.pools.ImageAnalysisPool;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;


public interface CollisionData extends ImageAnalysisPool.Analysis.Analyzable {

    @NotNull
    List<PointInt> getCollisionProfile();

    @NotNull
    List<PointInt> getSurfaceContourProfile();

    @NotNull
    default Stream<ModifiablePointInt> getSurfaceContourRelativeToOrigin() {
        final int rootX = getRootX(), rootY = getRootY();
        return getSurfaceContourProfile().stream()
                .map(p -> PointOld.of(p.x() + rootX, p.y() + rootY));
    }

    default void actionEachCollisionPoint(PrimitiveIntPairConsumer consumer) {
        final int rootX = getRootX(), rootY = getRootY();
        final List<PointInt> profile = getCollisionProfile();

        if (angleRad() == 0) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = profile.size(); i < n; i++) {
                consumer.accept(profile.get(i).x() + rootX, profile.get(i).y() + rootY);
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


                int x = profile.get(i).x();
                int y = profile.get(i).y();

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

    int getRootX();

    int getRootY();

}
