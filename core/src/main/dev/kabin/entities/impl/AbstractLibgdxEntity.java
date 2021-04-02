package dev.kabin.entities.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.impl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.impl.animation.AnimationBundleFactory;
import dev.kabin.util.pools.ImageAnalysisPool;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


abstract class AbstractLibgdxEntity implements EntityLibgdx {

    private static final Logger logger = Logger.getLogger(AbstractLibgdxEntity.class.getName());
    private static final AtomicInteger createdInstances = new AtomicInteger(1);

    // Protected data:
    private final AbstractAnimationPlaybackLibgdx<?> animationPlaybackImpl;

    // Class fields:
    private final String atlasPath;
    private final Actor actor = new Actor();
    private final int id;
    private final MutableRectInt positionNbd;
    private final RectIntView positionNbdView;
    private final MutableRectInt graphicsNbd;
    private final RectIntView graphicsNbdView;
    private float x, y, scale;
    private final EntityType type;

    // Class variables:
    private int layer;

    AbstractLibgdxEntity(EntityParameters parameters) {
        id = createdInstances.incrementAndGet();
        scale = parameters.scale();
        atlasPath = parameters.atlasPath();
        layer = parameters.layer();
        type = parameters.getType();
        animationPlaybackImpl = AnimationBundleFactory.loadFromAtlasPath(
                parameters.getTextureAtlas(),
                atlasPath,
                getType().animationClass()
        );
        if (animationPlaybackImpl != null) {
            animationPlaybackImpl.setSmoothParameter(0.5f);
        }

        actor.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return AbstractLibgdxEntity.this.touchDown(button);
            }
        });
        setPos(parameters.x(), parameters.y());
        {
            positionNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
            graphicsNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
            positionNbdView = new RectIntView(positionNbd);
            graphicsNbdView = new RectIntView(graphicsNbd);
            updateNeighborhood();
        }
    }

    @Override
    public final EntityType getType() {
        return type;
    }

    /**
     * @param clazz the guessed class of the animation playback of this instance.
     * @param <T>   class parameter.
     * @return the animation playback of this entity, or {@code null}, if the wrong class was provided.
     * @see #getAnimationPlaybackImpl()
     */
    @SuppressWarnings("unchecked") // Explicitly checked runtime.
    @Nullable
    protected final <T extends Enum<T>> AbstractAnimationPlaybackLibgdx<T> getAnimationPlaybackImpl(Class<T> clazz) {
        if (animationPlaybackImpl.getAnimationClass().isAssignableFrom(clazz)) {
            return (AbstractAnimationPlaybackLibgdx<T>) animationPlaybackImpl;
        } else return null;
    }

    /**
     * @return the animation playback of this instance.
     * @see #getAnimationPlaybackImpl(Class)
     */
    protected final AbstractAnimationPlaybackLibgdx<?> getAnimationPlaybackImpl() {
        return animationPlaybackImpl;
    }

    protected Actor actor() {
        return actor;
    }

    public boolean touchDown(int button) {
        logger.warning(() -> "Click registered.");
        switch (button) {
            case Input.Buttons.RIGHT -> handleMouseClickRight();
            case Input.Buttons.LEFT -> handleMouseClickLeft();
        }
        return true;
    }

    private void handleMouseClickLeft() {
    }

    private void handleMouseClickRight() {
    }

    @Override
    public void updateGraphics(GraphicsParametersLibgdx params) {
        setScale(params.getScale());

        final float graphicsRootX = getRootX();
        final float graphicsRootY = getRootY();


        animationPlaybackImpl.setX(graphicsRootX);
        animationPlaybackImpl.setY(graphicsRootY);
        animationPlaybackImpl.setScale(params.getScale());
        animationPlaybackImpl.renderNextAnimationFrame(params);

        // Configure actor.
        {
            final float offsetX = params.getCamX() - params.getScreenWidth() * 0.5f;
            final float offsetY = params.getCamY() - params.getScreenHeight() * 0.5f;
            final float x = graphicsRootX - offsetX;
            final float y = graphicsRootY - offsetY;
            actor.setBounds(
                    x, y,
                    animationPlaybackImpl.getWidth(),
                    animationPlaybackImpl.getHeight()
            );
        }

        // Updates nbds.
        updateNeighborhood();
    }

    private void updateNeighborhood() {
        graphicsNbd.translate(
                Math.round((getLeftmostPixel() + getX()) / getScale() - graphicsNbd.getCenterX()),
                Math.round((getHighestPixelFromBelow() + getY()) / getScale() - graphicsNbd.getCenterY())
        );

        positionNbd.translate(
                Math.round((getLeftmostPixel() + getX()) / getScale() - graphicsNbd.getCenterX()),
                Math.round((getHighestPixelFromBelow() + getY()) / getScale() - graphicsNbd.getCenterY())
        );
    }

    @Override
    public void updatePhysics(PhysicsParameters params) {

    }

    @Override
    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        actor.setX(x);
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        actor.setY(y);
        this.y = y;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        if (scale <= 0) throw new IllegalStateException("Scale should be positive.");

        if (scale != 0 && scale != this.scale) {
            x = x * scale / this.scale;
            y = y * scale / this.scale;
            this.scale = scale;
        } else if (this.scale == 0) {
            this.scale = scale;
        }
    }

    @Override
    public ImageAnalysisPool.Analysis getPixelAnalysis() {
        return animationPlaybackImpl != null ? animationPlaybackImpl.getPixelAnalysis() : ImageAnalysisPool.Analysis.emptyAnalysis();
    }

    @Override
    public Optional<Actor> getActor() {
        return Optional.of(actor);
    }

    @Override
    public RectIntView graphicsNbd() {
        return graphicsNbdView;
    }

    @Override
    public RectIntView positionNbd() {
        return positionNbdView;
    }

    @Override
    public JSONObject toJSONObject() {
        return new JSONObject()
                .put("x", getUnscaledX())
                .put("y", getUnscaledY())
                .put("atlasPath", atlasPath)
                .put("layer", getLayer())
                .put("type", getType().name());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int getMaxPixelHeight() {
        return animationPlaybackImpl.getMaxPixelHeight();
    }
}