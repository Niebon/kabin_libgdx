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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

class ModifyShaderWindow {

    private final int selectedLightSourceData = 0;
    private final EntityLibgdx e;

    ModifyShaderWindow(Stage stage,
                       EntitySelection es,
                       MouseEventUtil msu,
                       EntityLibgdx e) {
        this.e = e;

        if (e.getLightSourceData().isEmpty()) {
            e.getLightSourceData().add(AnchoredLightSourceData.ofNullables(LightSourceDataImpl.builder().build(), e::getX, e::getY));
        }

        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var window = new Window("Actions", skin);
        es.receiveDragListenerFrom(window);
        final float width = 250;
        final float height = 310;
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f, width, height);

        final int firstColumn = 60;
        final int secondColumn = 170;


        final var sliderX = makeSlider(firstColumn, 255, 100, 25, skin,
                -128, 128, 1.0f, false, getLsd().getUnscaledXRelToAnchor(), getLsd()::setUnscaledXRelToAnchor);
        window.addActor(sliderX);
        final var tfX = makeTextField(secondColumn, 255, 50, 25, skin,
                String.valueOf(getLsd().getUnscaledXRelToAnchor()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().setUnscaledXRelToAnchor(Float.parseFloat(s)));
        window.addActor(tfX);
        connect(sliderX, tfX, String::valueOf, Float::parseFloat);

        final var sliderY = makeSlider(firstColumn, 220, 100, 25, skin,
                -128, 128, 1.0f, false, getLsd().getUnscaledYRelToAnchor(), getLsd()::setUnscaledYRelToAnchor);
        window.addActor(sliderY);
        final var tfY = makeTextField(secondColumn, 220, 50, 25, skin,
                String.valueOf(getLsd().getUnscaledYRelToAnchor()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().setUnscaledYRelToAnchor(Float.parseFloat(s)));
        window.addActor(tfY);
        connect(sliderY, tfY, String::valueOf, Float::parseFloat);

        final var sliderR = makeSlider(firstColumn, 185, 100, 25, skin,
                0f, 264, 1.0f, false, getLsd().getUnscaledR(), getLsd()::setUnscaledR);
        window.addActor(sliderR);
        final var tfR = makeTextField(secondColumn, 185, 50, 25, skin,
                String.valueOf(getLsd().getR()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().setUnscaledR(Float.parseFloat(s)));
        window.addActor(tfR);
        connect(sliderR, tfR, String::valueOf, Float::parseFloat);

        final var red = makeSlider(firstColumn, 150, 100, 25, skin,
                0, 1, 1f / 255, false, getLsd().getTint().red(), getLsd().getTint()::setRed);
        window.addActor(red);
        final var tfred = makeTextField(secondColumn, 150, 50, 25, skin,
                String.valueOf(getLsd().getTint().red()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().setR(Float.parseFloat(s)));
        window.addActor(tfred);
        connect(red, tfred, String::valueOf, Float::parseFloat);

        final var green = makeSlider(firstColumn, 115, 100, 25, skin,
                0, 1, 1f / 255, false, getLsd().getTint().green(), getLsd().getTint()::setGreen);
        window.addActor(green);
        final var tfgreen = makeTextField(secondColumn, 115, 50, 25, skin,
                String.valueOf(getLsd().getTint().green()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().getTint().setGreen(Float.parseFloat(s)));
        window.addActor(tfgreen);
        connect(green, tfgreen, String::valueOf, Float::parseFloat);

        final var blue = makeSlider(firstColumn, 80, 100, 25, skin,
                0, 1, 1f / 255, false,
                getLsd().getTint().blue(),
                getLsd().getTint()::setBlue
        );
        window.addActor(blue);
        final var tfblue = makeTextField(secondColumn, 80, 50, 25, skin,
                String.valueOf(getLsd().getTint().blue()),
                (t, c) -> Character.isDefined(c) || c == '.',
                s -> getLsd().getTint().setBlue(Float.parseFloat(s)));
        window.addActor(tfblue);
        connect(blue, tfblue, String::valueOf, Float::parseFloat);


        final var lx = new Label("x:", skin);
        lx.setX(5);
        lx.setY(255);
        window.addActor(lx);


        final var ly = new Label("y:", skin);
        ly.setX(5);
        ly.setY(220);
        window.addActor(ly);


        final var lr = new Label("r:", skin);
        lr.setX(5);
        lr.setY(185);
        window.addActor(lr);


        final var lred = new Label("red:", skin);
        lred.setX(5);
        lred.setY(150);
        window.addActor(lred);


        final var lgreen = new Label("green:", skin);
        lgreen.setX(5);
        lgreen.setY(115);
        window.addActor(lgreen);


        final var lblue = new Label("blue:", skin);
        lblue.setX(5);
        lblue.setY(80);
        window.addActor(lblue);


        final var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(
                Arrays.stream(LightSourceType.values())
                        .map(Enum::name)
                        .toArray(String[]::new)
        );
        selectBox.setSelectedIndex(getLsd().getType().ordinal());
        selectBox.setWidth(100);
        selectBox.setHeight(25);
        selectBox.setX(firstColumn);
        selectBox.setY(45);
        selectBox.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                getLsd().setType(LightSourceType.values()[button]);
                return true;
            }
        });
        window.addActor(selectBox);


        final var changeButton = new TextButton("change", skin, "default");
        changeButton.setX(firstColumn);
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
        return e.getLightSourceData().get(selectedLightSourceData);
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private TextField makeTextField(
            float x, // 50
            float y, // 150
            float width, // 100
            float height, // 25
            Skin skin,
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
            Skin skin, float min,
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
