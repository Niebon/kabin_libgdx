package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import dev.kabin.entities.libgdximpl.*;
import dev.kabin.entities.libgdximpl.animation.AbstractAnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.AnimationBundleFactory;
import dev.kabin.entities.libgdximpl.animation.AnimationPlaybackLibgdx;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
import dev.kabin.util.functioninterfaces.FloatSupplier;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

class EntityLoadingWidget {

    // Constants:
    private static final int WIDTH = 600;
    private static final int HEIGHT = 200;

    // Class fields:
    private final dev.kabin.ui.Widget widget;
    private final Stage stage;
    private final FloatSupplier mouseRelativeToWorldX;
    private final FloatSupplier mouseRelativeToWorldY;
    private final FloatSupplier scale;
    private final Consumer<EntityLibgdx> registerEntityToWorld;
    private final Supplier<TextureAtlas> textureAtlasSupplier;
    private final Supplier<ImageMetadataPoolLibgdx> imageAnalysisPoolSupplier;


    // Class variables:
    private AbstractAnimationPlaybackLibgdx<?> preview = null;
    private String selectedAsset = "";
    private EntityType entityType = EntityType.ENTITY_ANIMATE; // Default.
    private Enum<?> animationType = entityType.animationClass().getEnumConstants()[0];
    private int layer;

    EntityLoadingWidget(
            Stage stage,
            Executor executor,
            FloatSupplier mouseRelativeToWorldX,
            FloatSupplier mouseRelativeToWorldY,
            FloatSupplier scale,
            Consumer<EntityLibgdx> registerEntityToWorld,
            Supplier<TextureAtlas> textureAtlasSupplier,
            Supplier<ImageMetadataPoolLibgdx> imageAnalysisPoolSupplier) {
        this.stage = stage;
        this.mouseRelativeToWorldX = mouseRelativeToWorldX;
        this.mouseRelativeToWorldY = mouseRelativeToWorldY;
        this.scale = scale;
        this.registerEntityToWorld = registerEntityToWorld;
        this.textureAtlasSupplier = textureAtlasSupplier;
        widget = new dev.kabin.ui.Widget.Builder()
                .setStage(stage)
                .setTitle("Entity loading widget")
                .setWidth(WIDTH)
                .setHeight(HEIGHT)
                .setCollapsedWindowWidth(WIDTH)
                .build();
        this.imageAnalysisPoolSupplier = imageAnalysisPoolSupplier;

        final var loadImageAssetButton = new TextButton("Asset", dev.kabin.ui.Widget.Builder.DEFAULT_SKIN, "default");
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
                showAnimationTypeBox(entityType.animationClass());
                return true;
            }
        });
        widget.addDialogActor(chooseAnimationTypeButton);

        refreshContentTableMessage();
    }

    private static <T extends Enum<T>> void setCurrentAnimationOfPreviewTo(
            AnimationPlaybackLibgdx<?> ap,
            Enum<?> animType
    ) {
        //noinspection unchecked
        ((AnimationPlaybackLibgdx<T>) ap).setCurrentAnimation((T) animType);
    }


    public JSONObject toJson() {
        return new JSONObject()
                .put("asset", selectedAsset.isEmpty() ? "player" : selectedAsset)
                .put("classification", entityType)
                .put("animation_type", animationType)
                .put("layer", layer)
                .put("collapsed", widget.isCollapsed());
    }


    public dev.kabin.ui.Widget getWidget() {
        return widget;
    }

    private void refreshContentTableMessage() {
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

    public Optional<EntityLibgdx> addEntity() {

        // Early exit, no asset selected.
        if (selectedAsset == null || selectedAsset.equals("")) {
            return Optional.empty();
        }


        final EntityParameters parameters = EntityParameters.builder()
                .setX(mouseRelativeToWorldX.get())
                .setY(mouseRelativeToWorldY.get())
                .setLayer(layer)
                .setScale(scale.get())
                .setAtlasPath(selectedAsset)
                .setTextureAtlas(textureAtlasSupplier.get())
                .setEntityType(entityType)
                .setImageAnalysisPool(imageAnalysisPoolSupplier.get())
                .build();


        final EntityLibgdx e = EntityType.Factory.parameterConstructorOf(entityType).construct(parameters);

        System.out.printf("Added an entity at (x,y) = (%s,%s)%n", e.getX(), e.getY());

        registerEntityToWorld.accept(e);
        e.getActor().ifPresent(stage::addActor);

        return Optional.of(e);
    }

    public void loadSettings(JSONObject settings) {
        selectedAsset = settings.getString("asset");
        entityType = EntityType.valueOf(settings.getString("type"));
        final String animationType = settings.getString("animation_type");
        this.animationType = getAnimationTypeByName(animationType, entityType.animationClass());
        layer = settings.getInt("layer");
        widget.setCollapsed(settings.getBoolean("collapsed"));


        // Finally, show content:
        preview = AnimationBundleFactory.loadFromAtlasPath(
                textureAtlasSupplier.get(),
                selectedAsset,
                imageAnalysisPoolSupplier.get(),
                entityType.animationClass().getEnumConstants()[0].getClass()
        );
        refreshContentTableMessage();
    }

    private Enum<?> getAnimationTypeByName(String animationType, Class<? extends Enum<?>> enumClass) {
        return Arrays
                .stream(enumClass.getEnumConstants())
                .map(e -> (Enum<?>) e)
                .filter(e -> e.name().equals(animationType))
                .findFirst()
                .orElseThrow(() -> new EnumConstantNotPresentException(entityType.animationClass(), animationType));
    }


    private void showSelectEntityTypeBox() {
        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(
                Arrays
                        .stream(EntityType.values())
                        .filter(classification -> classification.getGroupType() != EntityGroup.BACKGROUND)
                        .filter(classification -> classification.getGroupType() != EntityGroup.BACKGROUND_LAYER_2)
                        .filter(classification -> classification.getGroupType() != EntityGroup.SKY)
                        .map(Enum::name)
                        .toArray(String[]::new)
        );
        selectBox.setSelectedIndex(entityType.ordinal());
        final var dialog = new Dialog("Setting", skin);
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
                entityType = EntityType.valueOf(selectBox.getSelected());
                animationType = entityType.animationClass().getEnumConstants()[0];
                widget.removeActor(dialog);
                refreshContentTableMessage();
            }
        });
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
                preview = AnimationBundleFactory.loadFromAtlasPath(textureAtlasSupplier.get(),
                        selectedAsset,
                        imageAnalysisPoolSupplier.get(),
                        animationType.getClass());
            }
            refreshContentTableMessage();
        });
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    private void showAnimationTypeBox(Class<?> clazz) {
        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(Arrays.stream(clazz.getEnumConstants()).map(e -> (Enum<?>) e).map(Enum::name).toArray(String[]::new));
        selectBox.setSelectedIndex(0);
        final var dialog = new Dialog("Setting", skin);
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
                animationType = getAnimationTypeByName(selectBox.getSelected(), entityType.animationClass());
                refreshContentTableMessage();
                widget.removeActor(dialog);
            }
        });
    }

    public void render(GraphicsParametersLibgdx params) {
        if (widget.isVisible() && !widget.isCollapsed() && preview != null) {
            if (preview.getCurrentAnimation() != animationType) {
                setCurrentAnimationOfPreviewTo(preview, animationType);
            }
            float scale = 4.0f * 32 / preview.getOriginalWidth();
            preview.setScale(scale);
            preview.setPos(0.75f * WIDTH + widget.getX(), widget.getY());
            preview.renderNextAnimationFrame(params);
        }
    }

}
