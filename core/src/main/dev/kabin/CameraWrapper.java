package dev.kabin;

import com.badlogic.gdx.graphics.OrthographicCamera;
import dev.kabin.entities.libgdximpl.Player;
import dev.kabin.util.Functions;
import dev.kabin.util.WeightedAverage2D;
import dev.kabin.util.lambdas.FloatSupplier;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.NotNull;

public class CameraWrapper {

    // Bounds
    public final MutableRectInt currentCameraBounds = MutableRectInt.centeredAt(0, 0, GlobalData.ART_WIDTH, GlobalData.ART_HEIGHT);
    public final RectIntView currentCameraBoundsView = new RectIntView(currentCameraBounds);
    // Neighborhood
    public final MutableRectInt currentCameraNeighborhood = MutableRectInt.centeredAt(0, 0, 2 * GlobalData.ART_WIDTH, 2 * GlobalData.ART_HEIGHT);
    public final RectIntView currentCameraNeighborhoodView = new RectIntView(currentCameraNeighborhood);
    private final FloatSupplier scale;
    @NotNull
    private final OrthographicCamera camera;
    private final WeightedAverage2D directionalPreSmoothening = new WeightedAverage2D(0.1f);
    private final WeightedAverage2D directionalFinalSmoothening = new WeightedAverage2D(0.005f);


    public CameraWrapper(FloatSupplier scale,
                         @NotNull OrthographicCamera camera) {
        this.scale = scale;
        this.camera = camera;
    }

    public void setPos(float x, float y) {
        camera.position.set(x, y, camera.position.z);
        camera.update();
        // Find new camera position:
        currentCameraBounds.translate(
                Math.round(Functions.toIntDivideBy(x, scale.get()) - currentCameraBounds.getCenterX()),
                Math.round(Functions.toIntDivideBy(y, scale.get()) - currentCameraBounds.getCenterY())
        );
        currentCameraNeighborhood.translate(
                Math.round(Functions.toIntDivideBy(x, scale.get()) - currentCameraNeighborhood.getCenterX()),
                Math.round(Functions.toIntDivideBy(y, scale.get()) - currentCameraNeighborhood.getCenterY())
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

    public void follow(Player player) {
        final float unit = 3 * player.getMaxPixelHeight() * scale.get();
        directionalPreSmoothening.appendSignalX((float) (Math.signum(player.getVx()) * unit));
        directionalPreSmoothening.appendSignalY((float) (Math.signum(player.getVy()) * unit + 0.5f * unit));
        directionalFinalSmoothening.appendSignalX(directionalPreSmoothening.x());
        directionalFinalSmoothening.appendSignalY(directionalPreSmoothening.y());
        final float x = directionalFinalSmoothening.x();
        final float y = directionalFinalSmoothening.y();
        setPos(player.getX() + x, player.getY() + y);
    }

    public RectIntView currentCameraBounds() {
        return currentCameraBoundsView;
    }

    public RectIntView getCameraNeighborhood() {
        return currentCameraNeighborhoodView;
    }
}
