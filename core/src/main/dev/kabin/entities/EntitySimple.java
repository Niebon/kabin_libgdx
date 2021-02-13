package dev.kabin.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import dev.kabin.GlobalData;
import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.animation.AnimationPlaybackImpl;
import dev.kabin.ui.DeveloperUI;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import dev.kabin.utilities.shapes.primitive.MutableRectInt;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static dev.kabin.animation.AnimationPlaybackImpl.MOCK_ANIMATION_PLAYBACK;

public class EntitySimple implements Entity {

    private static final Logger LOGGER = Logger.getLogger(EntitySimple.class.getName());
    private static int createdInstances = 0;
    protected final AnimationPlaybackImpl<?> animationPlaybackImpl;
    private final String atlasPath;
    private final int layer;
    private final Actor actor = new Actor();
    private final int id;
    private final MutableRectInt positionNbd;
    private final MutableRectInt graphicsNbd;
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
                return EntitySimple.this.touchDown(button);
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

        // TODO: will the pathfinder algorithm suffer if position nbd is too big?
        positionNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
        graphicsNbd = MutableRectInt.centeredAt((int) getPixelMassCenterX(), (int) getPixelMassCenterY(), getPixelWidth(), getPixelHeight());
        updateNeighborhood();
        id = createdInstances++;
    }

    private void updateNeighborhood() {
        positionNbd.translate(
                getUnscaledX() - Math.round(positionNbd.getCenterX()),
                getUnscaledY() - Math.round(positionNbd.getCenterY())
        );
    }

    protected Actor actor() {
        return actor;
    }

    public boolean touchDown(int button) {
        LOGGER.warning(() -> "Click registered.");
        switch (button) {
            case Input.Buttons.RIGHT -> handleMouseClickRight();
            case Input.Buttons.LEFT -> handleMouseClickLeft();
        }
        return true;
    }

    private void handleMouseClickLeft() {
    }

    private void handleMouseClickRight() {
        if (GlobalData.developerMode) {
            final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            final var dialog = new Dialog("Actions", skin);
            final float width = 200;
            final float height = 200;
            dialog.setBounds(
                    MouseEventUtil.getXRelativeToUI() + width * 0.1f,
                    MouseEventUtil.getYRelativeToUI() + height * 0.1f,
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
                                EntityCollectionProvider.unregisterEntity(e);
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
    public void render(SpriteBatch batch, float stateTime) {
        animationPlaybackImpl.setX(x);
        animationPlaybackImpl.setY(y);
        animationPlaybackImpl.setScale(scale);
        animationPlaybackImpl.renderNextAnimationFrame(batch, stateTime);
        float offsetX = GlobalData.camera.position.x - GlobalData.screenWidth * 0.5f;
        float offsetY = GlobalData.camera.position.y - GlobalData.screenHeight * 0.5f;
        float x = this.x - offsetX;
        float y = this.y - offsetY;
        actor.setBounds(
                x, y,
                animationPlaybackImpl.getWidth(),
                animationPlaybackImpl.getHeight()
        );
    }

    @Override
    public void updatePhysics(PhysicsParameters params) {
        // Update x and y accordingly.
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
        this.scale = scale;
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
    public int getRootX() {
        return getUnscaledX() - getPixelAnalysis().getPixelMassCenterXInt();
    }

    @Override
    public int getRootY() {
        return getUnscaledY() - getPixelAnalysis().getPixelMassCenterYInt();
    }

    @Override
    public MutableRectInt graphicsNbd() {
        return graphicsNbd;
    }

    @Override
    public MutableRectInt positionNbd() {
        return positionNbd;
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
}