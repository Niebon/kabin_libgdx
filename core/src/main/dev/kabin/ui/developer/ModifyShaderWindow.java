package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.shaders.AnchoredLightSourceData;
import dev.kabin.shaders.LightSourceDataImpl;
import dev.kabin.shaders.LightSourceType;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import dev.kabin.util.fp.FloatSupplier;
import dev.kabin.util.fp.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

class ModifyShaderWindow {

    private final Stage stage;
    private final EntitySelection es;
    private final Skin skin;
    private final EntityLibgdx e;
    private final Window window;
    private int selectedLightSourceData = 0;

    ModifyShaderWindow(Stage stage,
                       EntitySelection es,
                       MouseEventUtil msu,
                       EntityLibgdx e) {
        this.stage = stage;
        this.es = es;
        this.e = e;

        if (e.getLightSourceDataList().isEmpty()) {
            e.getLightSourceDataList().add(AnchoredLightSourceData.ofNullables(LightSourceDataImpl.builder().build(), e::getX, e::getY));
        }

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


        final var buttonPrev = new TextButton("prev", skin);
        buttonPrev.setY(355);
        buttonPrev.setX(firstColumnX);
        buttonPrev.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                selectedLightSourceData--;
                return true;
            }
        });
        window.addActor(buttonPrev);

        final var buttonNext = new TextButton("next", skin);
        buttonNext.setY(355);
        buttonNext.setX(secondColumnX);
        window.addActor(buttonNext);


        addModifier("x",
                getLsd()::setUnscaledXRelToAnchor,
                getLsd()::getUnscaledXRelToAnchor,
                firstColumnX,
                secondColumnX,
                -128,
                128,
                1,
                325);

        addModifier("y",
                getLsd()::setUnscaledYRelToAnchor,
                getLsd()::getUnscaledYRelToAnchor,
                firstColumnX,
                secondColumnX,
                -128,
                128,
                1,
                290);

        addModifier("r",
                getLsd()::setUnscaledR,
                getLsd()::getUnscaledR,
                firstColumnX,
                secondColumnX,
                0,
                128,
                1,
                255);

        addModifier("angle",
                getLsd()::setAngle,
                getLsd()::getAngle,
                firstColumnX,
                secondColumnX,
                0,
                360,
                0.1f,
                220);

        addModifier("width",
                getLsd()::setWidth,
                getLsd()::getWidth,
                firstColumnX,
                secondColumnX,
                0,
                360,
                0.1f,
                185);

        addModifier("red",
                getLsd().getTint()::setRed,
                getLsd().getTint()::red,
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                150);

        addModifier("green",
                getLsd().getTint()::setGreen,
                getLsd().getTint()::green,
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                115);

        addModifier("blue",
                getLsd().getTint()::setBlue,
                getLsd().getTint()::blue,
                firstColumnX,
                secondColumnX,
                0,
                1,
                1f / 255,
                80);


        final var typeSelectBox = new SelectBox<String>(skin, "default");
        typeSelectBox.setItems(Arrays.stream(LightSourceType.values()).map(Enum::name).toArray(String[]::new));
        typeSelectBox.setSelectedIndex(getLsd().getType().ordinal());
        typeSelectBox.setWidth(100);
        typeSelectBox.setHeight(25);
        typeSelectBox.setX(firstColumnX);
        typeSelectBox.setY(45);
        typeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                getLsd().setType(LightSourceType.valueOf(typeSelectBox.getSelected()));
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
        final var exitButton = new TextButton("x", skin, "default");
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

    void makeAddShaderWindow() {
        var tempWindow = new Window("Add shader", skin);
        es.receiveDragListenerFrom(window);

    }

    private void addModifier(String description,
                             Consumer<Float> setter,
                             FloatSupplier getter,
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
                min, max, step, false, getter.get(), setter);
        window.addActor(sliderX);
        final var tfX = makeTextField(secondColumnX, y, 50, 25,
                String.valueOf(getter.get()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> setter.accept(Float.parseFloat(s)));
        window.addActor(tfX);
        connect(sliderX, tfX, String::valueOf, Float::parseFloat);
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

    private AnchoredLightSourceData getLsd() {
        return e.getLightSourceDataList().get(Math.floorMod(selectedLightSourceData, e.getLightSourceDataList().size()));
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private TextField makeTextField(
            float x, // 50
            float y, // 150
            float width, // 100
            float height, // 25
            String initialText,
            TextField.TextFieldFilter textFieldFilter,
            Consumer<String> action) {
        final var tf = new TextField(initialText, skin, "default");
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
        return tf;
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
            float value,
            Consumer<Float> action) {
        var slider = new Slider(min, max, step, vertical, skin);
        slider.setX(x);
        slider.setY(y);
        slider.setWidth(width);
        slider.setHeight(height);
        slider.setValue(value);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.accept(slider.getValue());
            }
        });
        return slider;
    }

}
