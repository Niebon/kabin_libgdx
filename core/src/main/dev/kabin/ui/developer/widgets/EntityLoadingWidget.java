package dev.kabin.ui.developer.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import dev.kabin.GlobalData;
import dev.kabin.MainGame;
import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.animation.AnimationClass;
import dev.kabin.animation.AnimationPlaybackImpl;
import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityParameters;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class EntityLoadingWidget {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 200;
    private final dev.kabin.ui.Widget widget;
    private AnimationPlaybackImpl<?> preview = null;
    private String selectedAsset = "";
    private EntityFactory.EntityType entityType = EntityFactory.EntityType.ENTITY_SIMPLE;
    private AnimationClass.Animate animationType = AnimationClass.Animate.DEFAULT_RIGHT;
    private int layer;

    public EntityLoadingWidget(Executor executor) {
        widget = new dev.kabin.ui.Widget.Builder()
                .setTitle("Entity loading widget")
                .setWidth(WIDTH)
                .setHeight(HEIGHT)
                .setCollapsedWindowWidth(WIDTH)
                .build();

        var loadImageAssetButton = new TextButton("Asset", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
        loadImageAssetButton.setWidth(100);
        loadImageAssetButton.setHeight(25);
        loadImageAssetButton.setX(25);
        loadImageAssetButton.setY(25);
        loadImageAssetButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                loadAsset(executor);
                return true;
            }

        });
        widget.addDialogActor(loadImageAssetButton);

        var chooseEntityTypeButton = new TextButton("Entity Type", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
        chooseEntityTypeButton.setWidth(100);
        chooseEntityTypeButton.setHeight(25);
        chooseEntityTypeButton.setX(25);
        chooseEntityTypeButton.setY(50);
        chooseEntityTypeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                showSelectEntityTypeBox();
                return true;
            }
        });
        widget.addDialogActor(chooseEntityTypeButton);

        var chooseAnimationTypeButton = new TextButton("Animation", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
        chooseAnimationTypeButton.setWidth(100);
        chooseAnimationTypeButton.setHeight(25);
        chooseAnimationTypeButton.setX(150);
        chooseAnimationTypeButton.setY(50);
        chooseAnimationTypeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                showAnimationTypeBox();
                return true;
            }
        });
        widget.addDialogActor(chooseAnimationTypeButton);

        refreshContentTableMessage();
    }

    public void loadSettings(JSONObject settings) {
        selectedAsset = settings.getString("asset");
        entityType = EntityFactory.EntityType.valueOf(settings.getString("type"));
        animationType = AnimationClass.Animate.valueOf(settings.getString("animation_type"));
        layer = settings.getInt("layer");
        widget.setCollapsed(settings.getBoolean("collapsed"));


        // Finally, show content:
        preview = AnimationBundleFactory.loadFromAtlasPath(selectedAsset);
        refreshContentTableMessage();
    }


    public JSONObject toJson() {
        return new JSONObject()
                .put("asset", selectedAsset.isEmpty() ? "player" : selectedAsset)
                .put("type", entityType)
                .put("animation_type", animationType)
                .put("layer", layer)
                .put("collapsed", widget.isCollapsed());
    }


    public dev.kabin.ui.Widget getWidget() {
        return widget;
    }

    void refreshContentTableMessage() {
        final int maxLength = 10;
        var contentTableMessage = new Label(
                "Asset: " +
                        (selectedAsset.length() < maxLength ? selectedAsset
                                : (selectedAsset.substring(0, maxLength) + "...")) +
                        '\n' +
                        "Entity primitiveType: " +
                        entityType.name() +
                        '\n' +
                        "Layer: " +
                        layer +
                        '\n' +
                        "Animation: " +
                        animationType.name(), dev.kabin.ui.Widget.Builder.DEFAULT_SKIN);
        contentTableMessage.setAlignment(Align.left);
        contentTableMessage.setPosition(25, 80);
        widget.refreshContentTableMessage(contentTableMessage);
    }

    public void addEntity() {

        // Early exit, no asset selected.
        if (selectedAsset == null || selectedAsset.equals("")) {
            return;
        }


        EntityParameters parameters = new EntityParameters.Builder()
                .setX(MouseEventUtil.getMouseXRelativeToWorld())
                .setY(MouseEventUtil.getMouseYRelativeToWorld())
                .setLayer(layer)
                .setScale(MainGame.scaleFactor)
                .setAtlasPath(selectedAsset)
                .build();


        Entity e = entityType.getParameterConstructor().construct(parameters);
        GlobalData.getWorldState().registerEntity(e);
        e.getActor().ifPresent(GlobalData.stage::addActor);

    }

    public void loadAsset(Executor executor) {
        executor.execute(() -> {
            final String relativePath = Gdx.files.getLocalStoragePath().replace("\\", "/")
                    + "core/assets/raw_textures/";
            JFileChooser chooser = new JFileChooser(relativePath);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            JFrame f = new JFrame();
            f.setVisible(true);
            f.toFront();
            f.setVisible(false);
            int res = chooser.showOpenDialog(f);
            f.dispose();
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                selectedAsset = selectedFile
                        .getAbsolutePath()
                        .replace("\\", "/")
                        .replace(relativePath, "");
                preview = AnimationBundleFactory.loadFromAtlasPath(selectedAsset);
            }
            refreshContentTableMessage();
        });
    }


    void showSelectEntityTypeBox() {
        var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name).toArray(String[]::new));
        selectBox.setSelectedIndex(entityType.ordinal());
        var dialog = new Dialog("Setting", skin);
        dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
        dialog.getContentTable().defaults().pad(10);
        dialog.getContentTable().add(selectBox);
        dialog.setSize(200, 200);
        dialog.getTitleTable().add(new TextButton("x", skin, "default"))
                .size(20, 20)
                .padRight(0).padTop(0);
        dialog.setModal(true);
        widget.addActor(dialog);
        dialog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entityType = EntityFactory.EntityType.valueOf(selectBox.getSelected());
                widget.removeActor(dialog);
                refreshContentTableMessage();
            }
        });
    }

    void showAnimationTypeBox() {
        var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(Arrays.stream(AnimationClass.Animate.values()).map(Enum::name).toArray(String[]::new));
        selectBox.setSelectedIndex(entityType.ordinal());
        var dialog = new Dialog("Setting", skin);
        dialog.setPosition(Gdx.graphics.getWidth() * 0.5f - 100, Gdx.graphics.getHeight() * 0.5f - 100);
        dialog.getContentTable().defaults().pad(10);
        dialog.getContentTable().add(selectBox);
        dialog.setSize(200, 200);
        dialog.getTitleTable().add(new TextButton("x", skin, "default"))
                .size(20, 20)
                .padRight(0).padTop(0);
        dialog.setModal(true);
        widget.addActor(dialog);
        dialog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                animationType = AnimationClass.Animate.valueOf(selectBox.getSelected());
                widget.removeActor(dialog);
            }
        });
    }

    public void setEntityType(EntityFactory.EntityType entityType) {
        this.entityType = entityType;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void render(SpriteBatch batch, float stateTime) {
        if (widget.isVisible() && !widget.isCollapsed() && preview != null) {
            if (preview.getCurrentAnimationType() != animationType) {
                preview.setCurrentAnimation(animationType);
            }
            float scale = 4.0f * 32 / preview.getOriginalWidth();
            preview.setScale(scale);
            preview.setPos(0.75f * WIDTH + widget.getX(), widget.getY());
            preview.renderNextAnimationFrame(batch, stateTime);
        }
    }

}
