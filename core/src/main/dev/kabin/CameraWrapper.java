package dev.kabin;

import com.badlogic.gdx.graphics.OrthographicCamera;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.geometry.primitive.MutableRectInt;
import dev.kabin.util.geometry.primitive.RectIntView;
import dev.kabin.util.lambdas.FloatSupplier;
import org.jetbrains.annotations.NotNull;

public class CameraWrapper {

    // Bounds
    public final MutableRectInt currentCameraBounds = MutableRectInt.centeredAt(0, 0, GlobalData.ART_WIDTH, GlobalData.ART_HEIGHT);
    public final RectIntView currentCameraBoundsView = new RectIntView(currentCameraBounds);
    // Neighborhood
    public final MutableRectInt currentCameraNeighborhood = MutableRectInt.centeredAt(0, 0, 2 * GlobalData.ART_WIDTH, 2 * GlobalData.ART_HEIGHT);
    public final RectIntView currentCameraNeighborhoodView = new RectIntView(currentCameraNeighborhood);
    private final FloatSupplier scaleX;
    private final FloatSupplier scaleY;
    @NotNull
    private final OrthographicCamera camera;
    private final WeightedAverage2D directionalPreSmoothening = new WeightedAverage2D(0.1f);
    private final WeightedAverage2D directionalFinalSmoothening = new WeightedAverage2D(0.005f);


    public CameraWrapper(FloatSupplier scaleX,
                         FloatSupplier scaleY,
                         @NotNull OrthographicCamera camera) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.camera = camera;
    }

    public void setPos(float x, float y) {
        camera.position.set(x, y, camera.position.z);
        camera.update();
        // Find new camera position:
        currentCameraBounds.translate(
                Math.round(x / scaleX.get() - currentCameraBounds.getCenterX()),
                Math.round(y / scaleY.get() - currentCameraBounds.getCenterY())
        );
        currentCameraNeighborhood.translate(
                Math.round(x / scaleX.get() - currentCameraNeighborhood.getCenterX()),
                Math.round(y / scaleY.get() - currentCameraNeighborhood.getCenterY())
        );
    }

    public float getCameraX() {
        return camera.position.x;
    }

    public float getCameraY() {
        return camera.position.y;
    }

    @NotNull
    public OrthographicCamera getCamera() {
        return camera;
    }

    private float scale() {
        return scaleX.get();
    }

    public void follow(Player player, float timeElapsedSinceLastFrame) {
        final float unit = 3 * player.getMaxPixelHeight() * scale();
        directionalPreSmoothening.appendSignalX((float) (Math.signum(player.getVx() * timeElapsedSinceLastFrame) * unit));
        directionalPreSmoothening.appendSignalY((float) (Math.signum(player.getVy() * timeElapsedSinceLastFrame) * unit + 0.5f * unit));
        directionalFinalSmoothening.appendSignalX(directionalPreSmoothening.x());
        directionalFinalSmoothening.appendSignalY(directionalPreSmoothening.y());
        final float x = directionalFinalSmoothening.x();
        final float y = directionalFinalSmoothening.y();
        setPos(player.x() * scale() + x, player.y() * scale() + y);
    }

    public RectIntView currentCameraBounds() {
        return currentCameraBoundsView;
    }

    public RectIntView getCameraNeighborhood() {
        return currentCameraNeighborhoodView;
    }
}
