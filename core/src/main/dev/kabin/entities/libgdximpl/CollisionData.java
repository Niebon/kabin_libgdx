package dev.kabin.entities.libgdximpl;

import dev.kabin.util.geometry.points.PointInt;
import dev.kabin.util.lambdas.PrimitiveIntPairConsumer;
import dev.kabin.util.pools.imagemetadata.ImgMetadataDelegator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;


public interface CollisionData extends ImgMetadataDelegator {

    @NotNull
    List<PointInt> getCollisionProfile();

    @NotNull
    List<PointInt> getSurfaceContourProfile();

    @NotNull
    default Stream<PointInt> getSurfaceContourRelativeToOrigin() {
        final int rootX = getRootXAsInt(), rootY = getRootYAsInt();
        return getSurfaceContourProfile().stream()
                .map(p -> PointInt.immutable(p.x() + rootX, p.y() + rootY));
    }

    default void actionEachCollisionPoint(PrimitiveIntPairConsumer consumer) {
        final int rootX = getRootXAsInt(), rootY = getRootYAsInt();
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
            int pixelMassCenterX = imgMetadata().artMassCenterXAsInt();
            int pixelMassCenterY = imgMetadata().artMassCenterYAsInt();

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
     * @return the orientation of this collision data.
     */
    default double angleRad() {
        return 0;
    }

    int getRootXAsInt();

    int getRootYAsInt();

}
