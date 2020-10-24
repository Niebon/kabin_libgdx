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
import dev.kabin.animation.AnimatedGraphicsAsset;
import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.global.GlobalData;
import dev.kabin.ui.DeveloperUI;
import dev.kabin.utilities.eventhandlers.MouseEventUtil;
import dev.kabin.utilities.pools.ImageAnalysisPool;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class EntitySimple implements Entity {

    private static final Logger logger = Logger.getLogger(EntitySimple.class.getName());

    protected final AnimatedGraphicsAsset<?> animatedGraphicsAsset;
    private final String atlasPath;
    private final int layer;
    private final Actor actor = new Actor();
    private float x, y, scale;

    protected Actor actor(){
        return actor;
    }

    EntitySimple(EntityParameters parameters) {
        scale = parameters.scale();
        atlasPath = parameters.atlasPath();
        layer = parameters.layer();
        animatedGraphicsAsset = AnimationBundleFactory.loadFromAtlasPath(atlasPath);
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
        if (GlobalData.developerMode) {
            final Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
            var dialog = new Dialog("Actions", skin);
            float width = 200;
            float height = 200;
            dialog.setBounds(
                    MouseEventUtil.getMouseX() + width * 0.1f,
                    MouseEventUtil.getMouseY() + height * 0.1f,
                    width, height
            );
            dialog.getContentTable().defaults().pad(10);

            // Remove button.
            var removeButton = new TextButton("Remove", skin, "default");
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
                                EntityGroupProvider.unregisterEntity(e);
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
        animatedGraphicsAsset.setX(x);
        animatedGraphicsAsset.setY(y);
        animatedGraphicsAsset.setScale(scale);
        animatedGraphicsAsset.renderNextAnimationFrame(batch, stateTime);
        actor.setBounds(
                x, y,
                animatedGraphicsAsset.getWidth(),
                animatedGraphicsAsset.getHeight()
        );
    }

    @Override
    public void updatePhysics() {
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
        return ImageAnalysisPool.findAnalysis(animatedGraphicsAsset.getCurrentImageAssetPath(), animatedGraphicsAsset.getCurrentImageAssetIndex());
    }

    @Override
    public Optional<Actor> getActor() {
        return Optional.of(actor);
    }

    @Override
    public int getRootX() {
        return 0;
    }

    @Override
    public int getRootY() {
        return 0;
    }

    @Override
    public JSONObject toJSONObject() {
        return new JSONObject()
                .put("x", Math.round(getX() / getScale()))
                .put("y", Math.round(getY() / getScale()))
                .put("atlasPath", getAtlasPath())
                .put("layer", getLayer())
                .put("tileType", getType().name());
    }
}