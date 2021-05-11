package dev.kabin.entities.libgdximpl;

import com.badlogic.gdx.scenes.scene2d.Actor;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.AnimationBundleFactory;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataLibgdx;
import dev.kabin.shaders.AnchoredLightSourceData;
import dev.kabin.util.NamedObj;
import dev.kabin.util.collections.LazyList;
import dev.kabin.util.lambdas.BiFunction;
import dev.kabin.util.pools.imagemetadata.ImageMetadata;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


abstract class AbstractLibgdxEntity implements EntityLibgdx {

    private static final AtomicInteger createdInstances = new AtomicInteger();

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
    private final EntityType type;
    private final List<NamedObj<AnchoredLightSourceData>> namedLightSourceDataList;
    private final LazyList<AnchoredLightSourceData> lightSourceDataList;

    // Class variables:
    private float x;
    private float y;
    private int layer;

    AbstractLibgdxEntity(EntityParameters parameters) {
        id = createdInstances.incrementAndGet();
        atlasPath = parameters.atlasPath();
        layer = parameters.layer();
        type = parameters.type();
        animationPlaybackImpl = AnimationBundleFactory.loadFromAtlasPath(
                parameters.textureAtlas(),
                atlasPath,
                parameters.imageAnalysisPool(),
                getType().animationClass()
        );
        if (animationPlaybackImpl != null) {
            animationPlaybackImpl.setSmoothParameter(0.5f);
        }
        setPos(parameters.x(), parameters.y());
        {
            positionNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
            graphicsNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
            positionNbdView = new RectIntView(positionNbd);
            graphicsNbdView = new RectIntView(graphicsNbd);
            updateNeighborhood();
        }
        namedLightSourceDataList = parameters.lightSourceData().stream()
                .map(namedLsd -> namedLsd.map(l -> AnchoredLightSourceData.ofNullables(l, this::getX, this::getY)))
                .collect(Collectors.toCollection(ArrayList::new));
        lightSourceDataList = new LazyList<>(i -> namedLightSourceDataList.get(i).obj(), namedLightSourceDataList::size);
    }

    @UnmodifiableView
    @Override
    public final LazyList<AnchoredLightSourceData> getLightSourceDataList() {
        return lightSourceDataList;
    }

    @Override
    public Map<String, AnchoredLightSourceData> getLightSourceDataMap() {
        return namedLightSourceDataList.stream().collect(Collectors.toMap(NamedObj::name,
                NamedObj::obj,
                BiFunction::projectLeft,
                // Yields a sorted output.
                TreeMap::new));
    }

    @Override
    public void addLightSourceData(String name, AnchoredLightSourceData lightSourceData) {
        if (namedLightSourceDataList.stream().noneMatch(nlsd -> nlsd.name().equals(name))) {
            namedLightSourceDataList.add(new NamedObj<>(name, lightSourceData));
        }
    }

    @Override
    public void removeLightSourceData(String name) {
        namedLightSourceDataList.removeIf(nlsd -> nlsd.name().equals(name));
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
        if (animationPlaybackImpl != null && animationPlaybackImpl.getAnimationClass().isAssignableFrom(clazz)) {
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

    @Override
    public void updateGraphics(GraphicsParametersLibgdx params) {

        final float graphicsRootX = getRootX();
        final float graphicsRootY = getRootY();


        animationPlaybackImpl.setPos(graphicsRootX, graphicsRootY);

        // Sets the canonical shader for the group of this.
        animationPlaybackImpl.setShaderProgram(params.shaderFor(getGroupType()));
        animationPlaybackImpl.renderNextAnimationFrame(params);

        // Configure actor.
        {
            final float offsetX = params.camX() - params.screenWidth() * 0.5f;
            final float offsetY = params.camY() - params.screenHeight() * 0.5f;
            final float x = graphicsRootX * params.scale() - offsetX;
            final float y = graphicsRootY * params.scale() - offsetY;
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
                Math.round(getLeftmostPixel() + getX() - graphicsNbd.getCenterX()),
                Math.round(getHighestPixelFromBelow() + getY() - graphicsNbd.getCenterY())
        );

        positionNbd.translate(
                Math.round(getLeftmostPixel() + getX() - graphicsNbd.getCenterX()),
                Math.round(getHighestPixelFromBelow() + getY() - graphicsNbd.getCenterY())
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
    public ImageMetadata getImageMetadata() {
        return animationPlaybackImpl != null ? animationPlaybackImpl.getPixelAnalysis() : ImageMetadataLibgdx.emptyAnalysis();
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
                .put("x", getXAsInt())
                .put("y", getYAsInt())
                .put("atlas_path", atlasPath)
                .put("layer", getLayer())
                .put("type", getType().name())
                .put("light_sources", namedLightSourceDataList.stream().collect(Collectors.toMap(NamedObj::name, NamedObj::obj)));
    }

    @Override
    public int id() {
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

    @Override
    public int getAvgLowestPixel() {
        return animationPlaybackImpl.getAvgLowestPixel();
    }


    @Override
    public float getAvgMassCenterX() {
        return animationPlaybackImpl.getAvgMassCenterX();
    }

    @Override
    public float getAvgMassCenterY() {
        return animationPlaybackImpl.getAvgMassCenterY();
    }

}