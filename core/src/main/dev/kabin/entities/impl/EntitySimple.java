package dev.kabin.entities.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import dev.kabin.GlobalData;
import dev.kabin.entities.GraphicsParameters;
import dev.kabin.entities.PhysicsParameters;
import dev.kabin.entities.animation.AnimationBundleFactory;
import dev.kabin.entities.animation.AnimationPlaybackImpl;
import dev.kabin.ui.developer.DeveloperUI;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.pools.ImageAnalysisPool;
import dev.kabin.util.shapes.primitive.MutableRectInt;
import dev.kabin.util.shapes.primitive.RectIntView;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static dev.kabin.entities.animation.AnimationPlaybackImpl.MOCK_ANIMATION_PLAYBACK;


public class EntitySimple implements Entity {

    private static final Logger LOGGER = Logger.getLogger(EntitySimple.class.getName());
    private static final AtomicInteger createdInstances = new AtomicInteger(1);
    protected final AnimationPlaybackImpl<?> animationPlaybackImpl;
    private final String atlasPath;
    private int layer;
    private final Actor actor = new Actor();
    private final int id;
    private final MutableRectInt positionNbd;
    private final RectIntView positionNbdView;
    private final MutableRectInt graphicsNbd;
    private final RectIntView graphicsNbdView;

    private float x, y, scale;

    EntitySimple(EntityParameters parameters) {
        scale = parameters.scale();
        atlasPath = parameters.atlasPath();
        layer = parameters.layer();
        switch (parameters.getContext()) {
            case TEST -> animationPlaybackImpl = MOCK_ANIMATION_PLAYBACK;
            case PRODUCTION -> animationPlaybackImpl = AnimationBundleFactory.loadFromAtlasPath(atlasPath);
            default -> throw new IllegalStateException("Unexpected value: " + parameters.getContext());
        }
        actor.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return EntitySimple.this.touchDown(button, x, y);
            }
        });
        actor.addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (DeveloperUI.getEntitySelection().getCurrentlySelectedEntities().isEmpty()) {
                    DeveloperUI.addEntityToDraggedEntities(EntitySimple.this);
                } else {
                    DeveloperUI.getEntitySelection().getCurrentlySelectedEntities()
                            .forEach(DeveloperUI::addEntityToDraggedEntities);
                }
            }
        });

        setPos(parameters.x(), parameters.y());

        positionNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
        graphicsNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
        positionNbdView = new RectIntView(positionNbd);
        graphicsNbdView = new RectIntView(graphicsNbd);
        updateNeighborhood();
        id = createdInstances.incrementAndGet();
    }

    protected Actor actor() {
        return actor;
    }

    public boolean touchDown(int button, float x, float y) {
        LOGGER.warning(() -> "Click registered.");
        switch (button) {
            case Input.Buttons.RIGHT -> handleMouseClickRight(x, y);
            case Input.Buttons.LEFT -> handleMouseClickLeft();
        }
        return true;
    }

    private void handleMouseClickLeft() {
    }

    private void handleMouseClickRight(float x, float y) {
        if (GlobalData.developerMode) {
            final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            final var dialog = new Dialog("Actions", skin);
            final float width = 200;
            final float height = 200;
            dialog.setBounds(
                    x + width * 0.1f,
                    y + height * 0.1f,
                    width, height
            );
            dialog.getContentTable().defaults().pad(10);

            // Remove button.
            final var removeButton = new TextButton("Remove", skin, "default");
            removeButton.addListener(
                    new ClickListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                            // Find all dev.kabin.entities scheduled for removal.
                            final Set<Entity> entitiesScheduledForRemoval = new HashSet<>();
                            final Set<Entity> currentlySelectedEntities = DeveloperUI.getEntitySelection()
                                    .getCurrentlySelectedEntities();
                            if (currentlySelectedEntities.contains(EntitySimple.this)) {
                                entitiesScheduledForRemoval.addAll(currentlySelectedEntities);
                            } else {
                                entitiesScheduledForRemoval.add(EntitySimple.this);
                            }

                            entitiesScheduledForRemoval.forEach(e -> {
                                GlobalData.getWorldState().unregisterEntity(e);
                                e.getActor().ifPresent(Actor::remove);
                            });

                            dialog.remove();
                            return true;
                        }
                    }
            );
            dialog.getContentTable().add(removeButton).size(100, 30);

            // Exit button.
            var exitButton = new TextButton("x", skin, "default");
            exitButton.addListener(
                    new ClickListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            return dialog.remove();
                        }
                    }
            );
            dialog.getTitleTable().add(exitButton)
                    .size(20, 20)
                    .padRight(0).padTop(0);
            dialog.setModal(true);
            GlobalData.stage.addActor(dialog);
        }
    }

    @Override
    public void updateGraphics(GraphicsParameters params) {
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
                Math.round((getLeftmostPixel() + getX())/ getScale() - graphicsNbd.getCenterX()),
                Math.round((getHighestPixelFromBelow() + getY()) / getScale() - graphicsNbd.getCenterY())
        );

        positionNbd.translate(
                Math.round((getLeftmostPixel() + getX())/ getScale() - graphicsNbd.getCenterX()),
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
    public String getAtlasPath() {
        return atlasPath;
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.ENTITY_SIMPLE;
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
        return animationPlaybackImpl.getPixelAnalysis();
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
                .put("atlasPath", getAtlasPath())
                .put("layer", getLayer())
                .put("primitiveType", getType().name());
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

    public void setLayer(int layer) {
        this.layer = layer;
    }
}