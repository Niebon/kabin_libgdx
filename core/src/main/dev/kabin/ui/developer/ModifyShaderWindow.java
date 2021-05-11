package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.Cooldown;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.shaders.*;
import dev.kabin.util.Lists;
import dev.kabin.util.NamedObj;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.lambdas.Function;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ModifyShaderWindow {


    private final Skin skin;
    private final EntityLibgdx e;
    private final Window window;
    private final ArrayList<Runnable> refreshRunnables = new ArrayList<Runnable>();
    private final Cooldown addNewShaderCooldown = new Cooldown(100);
    private String currLightSourceData;


    ModifyShaderWindow(Stage stage,
                       EntitySelection es,
                       MouseEventUtil msu,
                       EntityLibgdx e) {
        this.e = e;

        currLightSourceData = e.getLightSourceDataMap().keySet().stream().findFirst().orElse(null);


        skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        window = new Window("Actions", skin);
        es.receiveDragListenerFrom(window);
        final float width = 250;
        final float height = 415;
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f, width, height);

        final int firstColumnX = 60;
        final int secondColumnX = 170;


        final var desc = new Label("select", skin);
        desc.setX(5);
        desc.setY(355);
        window.addActor(desc);

        final var selectBoxLightData = new SelectBox<String>(skin);
        final var items = Lists.concat(e.getLightSourceDataMap().keySet(), "--new--");
        selectBoxLightData.setItems(items.toArray(String[]::new));
        selectBoxLightData.setX(firstColumnX);
        selectBoxLightData.setY(355);
        selectBoxLightData.setWidth(90);
        selectBoxLightData.setHeight(25);
        selectBoxLightData.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (addNewShaderCooldown.isReady() && selectBoxLightData.getSelected().equals("--new--")) {
                    float width = 200, height = 100;

                    var dialogSetName = new Dialog("Set name", skin);

                    var tfSetName = new TextField("", skin);
                    tfSetName.setX(width * 0.25f);
                    tfSetName.setY(height * 0.25f + 5f);
                    tfSetName.setWidth(100f);
                    tfSetName.setTextFieldFilter((textField, c) -> Character.isAlphabetic(c) || Character.isDigit(c) || c == '_');
                    dialogSetName.addActor(tfSetName);
                    es.receiveDragListenerFrom(dialogSetName);

                    var tb = new TextButton("Ok", skin);
                    tb.setX(width * 0.25f);
                    tb.setY(5f);
                    tb.setHeight(25f);
                    tb.setWidth(100f);
                    tb.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            String text = tfSetName.getText();
                            if (text.length() > 3) {
                                e.addLightSourceData(text, AnchoredLightSourceData.ofNullables(LightSourceDataImpl.builder().build(), e::getX, e::getY));
                                final var items = Lists.concat(e.getLightSourceDataMap().keySet(), "--new--");
                                selectBoxLightData.setItems(items.toArray(String[]::new));
                                selectBoxLightData.setSelected(text);
                                dialogSetName.remove();
                                refreshRunnables.forEach(Runnable::run);
                                addNewShaderCooldown.trigger();
                            }
                        }
                    });
                    dialogSetName.addActor(tb);


                    // Exit button.
                    var exitButton = new TextButton("x", skin);
                    exitButton.addListener(
                            new ClickListener() {
                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                    return dialogSetName.remove();
                                }
                            }
                    );
                    dialogSetName.getTitleTable().add(exitButton).size(20, 20).padRight(0).padTop(0);
                    dialogSetName.setModal(true);
                    dialogSetName.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f, width, height);
                    stage.addActor(dialogSetName);
                } else {
                    currLightSourceData = selectBoxLightData.getSelected();
                }
                refreshAll();
            }
        });
        window.addActor(selectBoxLightData);

        final var deleteLightSource = new TextButton("delete", skin);
        deleteLightSource.setX(firstColumnX + 100);
        deleteLightSource.setY(355);
        deleteLightSource.setHeight(25);
        deleteLightSource.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                var cw = new Dialog("Confirmation", skin); // confirmation window
                es.receiveDragListenerFrom(cw);
                float width = 175, height = 100;

                var yes = new TextButton("yes", skin);
                yes.setX(25);
                yes.setY(25);
                yes.setWidth(50);
                yes.addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        addNewShaderCooldown.trigger();
                        e.removeLightSourceData(currLightSourceData);
                        final var items = Lists.concat(e.getLightSourceDataMap().keySet(), "--new--");
                        selectBoxLightData.setItems(items.toArray(String[]::new));
                        selectBoxLightData.setSelected(items.size() > 1 ? items.get(0) : null);
                        return cw.remove();
                    }
                });
                cw.addActor(yes);


                var cancel = new TextButton("cancel", skin);
                cancel.setX(100);
                cancel.setY(25);
                cancel.setWidth(50);
                cancel.addListener(new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return cw.remove();
                    }
                });
                cw.addActor(cancel);

                stage.addActor(cw);
                // Exit button.
                var exitButton = new TextButton("x", skin);
                exitButton.addListener(
                        new ClickListener() {
                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                return cw.remove();
                            }
                        }
                );
                cw.getTitleTable().add(exitButton).size(20, 20).padRight(0).padTop(0);
                cw.setModal(true);
                cw.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f, width, height);
                return true;
            }
        });
        window.addActor(deleteLightSource);


        addModifier("x",
                f -> getLsd().ifPresent(lsd -> lsd.setUnscaledXRelToAnchor(f)),
                () -> getLsd().map(AnchoredLightSourceData::getUnscaledXRelToAnchor).orElse(0f),
                firstColumnX,
                secondColumnX,
                -128,
                128,
                1,
                325);

        addModifier("y",
                f -> getLsd().ifPresent(lsd -> lsd.setUnscaledYRelToAnchor(f)),
                () -> getLsd().map(AnchoredLightSourceData::getUnscaledYRelToAnchor).orElse(0f),
                firstColumnX,
                secondColumnX,
                -128,
                128,
                1,
                290);

        addModifier("r",
                f -> getLsd().ifPresent(lsd -> lsd.setR(f)),
                () -> getLsd().map(AnchoredLightSourceData::getUnscaledR).map(Integer::floatValue).orElse(32f),
                firstColumnX,
                secondColumnX,
                0,
                128,
                1,
                255);

        addModifier("angle",
                f -> getLsd().ifPresent(lsd -> lsd.setAngle(f)),
                () -> getLsd().map(AnchoredLightSourceData::getAngle).orElse(0f),
                firstColumnX,
                secondColumnX,
                0,
                360,
                0.1f,
                220);

        addModifier("width",
                f -> getLsd().ifPresent(lsd -> lsd.setArcSpan(f)),
                () -> getLsd().map(AnchoredLightSourceData::getArcSpan).orElse(0f),
                firstColumnX,
                secondColumnX,
                0,
                360,
                0.1f,
                185);

        addModifier("red",
                f -> getLsd().ifPresent(lsd -> lsd.getTint().setRed(f)),
                () -> getLsd().map(LightSourceData::getTint).map(Tint::red).orElse(1f),
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                150);

        addModifier("green",
                f -> getLsd().ifPresent(lsd -> lsd.getTint().setGreen(f)),
                () -> getLsd().map(LightSourceData::getTint).map(Tint::red).orElse(1f),
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                115);

        addModifier("blue",
                f -> getLsd().ifPresent(lsd -> lsd.getTint().setBlue(f)),
                () -> getLsd().map(LightSourceData::getTint).map(Tint::red).orElse(1f),
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                80);


        final var typeSelectBox = new SelectBox<String>(skin, "default");
        typeSelectBox.setItems(Arrays.stream(LightSourceType.values()).map(Enum::name).toArray(String[]::new));
        typeSelectBox.setSelectedIndex(getLsd().map(AnchoredLightSourceData::getType).map(Enum::ordinal).orElse(0));
        typeSelectBox.setWidth(100);
        typeSelectBox.setHeight(25);
        typeSelectBox.setX(firstColumnX);
        typeSelectBox.setY(45);
        typeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getLsd().ifPresent(lsd -> lsd.setType(LightSourceType.valueOf(typeSelectBox.getSelected())));
            }
        });
        window.addActor(typeSelectBox);


        final var changeButton = new TextButton("change", skin, "default");
        changeButton.setX(firstColumnX);
        changeButton.setY(10);
        changeButton.setWidth(100);
        changeButton.setHeight(25);
        changeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                window.remove();
                es.removeDragListenerTo(window);
                return true;
            }
        });
        window.addActor(changeButton);

        // Exit button.
        final var exitButton = new TextButton("x", skin);
        exitButton.addListener(
                new ClickListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return window.remove();
                    }
                }
        );
        window.getTitleTable().add(exitButton)
                .size(20, 20)
                .padRight(0).padTop(0);
        window.setModal(true);
        stage.addActor(window);
    }

    private static void connect(Slider s,
                                TextField tf,
                                Function<Float, String> floatToString,
                                Function<String, Float> stringToFloat
    ) {
        s.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tf.setText(floatToString.apply(s.getValue()));
            }
        });
        tf.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                s.setValue(stringToFloat.apply(tf.getText()));
            }
        });
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue"})
    private void addModifier(String description,
                             Consumer<Float> setter,
                             Supplier<Float> getter,
                             int firstColumnX,
                             int secondColumnX,
                             float min,
                             float max,
                             float step,
                             int y) {
        final var desc = new Label(description, skin);
        desc.setX(5);
        desc.setY(y);
        window.addActor(desc);
        final var sliderX = makeSlider(firstColumnX, y, 100, 25,
                min, max, step, false, getter, setter);
        window.addActor(sliderX);
        final var tfX = makeTextField(secondColumnX, y, 50, 25,
                () -> String.valueOf(getter.get()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> setter.accept(Float.parseFloat(s)));
        window.addActor(tfX);
        connect(sliderX, tfX, String::valueOf, Float::parseFloat);
    }

    private Optional<AnchoredLightSourceData> getLsd() {
        return getNamedLsd().map(NamedObj::obj);
    }

    private Optional<NamedObj<AnchoredLightSourceData>> getNamedLsd() {
        return Optional.ofNullable(currLightSourceData).map(lsd -> new NamedObj<>(currLightSourceData, e.getLightSourceDataMap().get(lsd)));
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private TextField makeTextField(
            float x, // 50
            float y, // 150
            float width, // 100
            float height, // 25
            Supplier<String> initialText,
            TextField.TextFieldFilter textFieldFilter,
            Consumer<String> action) {
        final var tf = new TextField(initialText.get(), skin, "default");
        tf.setTextFieldFilter(textFieldFilter);
        tf.setX(x);
        tf.setY(y);
        tf.setWidth(width);
        tf.setHeight(height);
        tf.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.accept(tf.getText());
            }
        });
        refreshRunnables.add(() -> tf.setText(initialText.get()));
        refreshAll();
        return tf;
    }

    void refreshAll() {
        refreshRunnables.forEach(Runnable::run);
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private Slider makeSlider(
            float x, // 50
            float y, // 150
            float width, // 100
            float height, // 25,
            float min,
            float max,
            float step,
            boolean vertical,
            Supplier<Float> getter,
            Consumer<Float> action) {
        var slider = new Slider(min, max, step, vertical, skin);
        slider.setX(x);
        slider.setY(y);
        slider.setWidth(width);
        slider.setHeight(height);
        slider.setValue(getter.get());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.accept(slider.getValue());
            }
        });
        refreshRunnables.add(() -> slider.setValue(getter.get()));
        return slider;
    }

}
