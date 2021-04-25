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
import dev.kabin.shaders.LightSourceType;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

class ModifyShaderWindow {

    ModifyShaderWindow(Stage stage,
                       EntitySelection es,
                       MouseEventUtil msu,
                       EntityLibgdx e) {

        final AnchoredLightSourceData lsd = e.getLightSourceData();

        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var window = new Window("Actions", skin);
        es.receiveDragListenerFrom(window);
        final float width = 250;
        final float height = 300;
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f, width, height);

        final int columnX = 60;

        final var sliderX = makeSlider(60, 255, 100, 25, skin,
                -128, 128, 1.0f, false, lsd.getUnscaledXRelToAnchor(), lsd::setXRelToAnchor);
        window.addActor(sliderX);

        final var sliderY = makeSlider(60, 220, 100, 25, skin,
                -128, 128, 1.0f, false, lsd.getUnscaledYRelToAnchor(), lsd::setYRelToAnchor);
        window.addActor(sliderY);

        final var sliderR = makeSlider(60, 185, 100, 25, skin,
                0f, 264, 1.0f, false, lsd.getR(), lsd::setR);
        window.addActor(sliderR);


        final var red = makeSlider(columnX, 150, 100, 25, skin,
                0, 1, 1f / 255, false,
                lsd.getTint().red(),
                lsd.getTint()::setRed
        );
        window.addActor(red);

        final var green = makeSlider(columnX, 115, 100, 25, skin,
                0, 1, 1f / 255, false,
                lsd.getTint().green(),
                lsd.getTint()::setGreen
        );
        window.addActor(green);

        final var blue = makeSlider(columnX, 80, 100, 25, skin,
                0, 1, 1f / 255, false,
                lsd.getTint().blue(),
                lsd.getTint()::setBlue
        );
        window.addActor(blue);


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
        selectBox.setSelectedIndex(lsd.getType().ordinal());
        selectBox.setWidth(100);
        selectBox.setHeight(25);
        selectBox.setX(columnX);
        selectBox.setY(45);
        selectBox.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lsd.setType(LightSourceType.values()[button]);
                return true;
            }
        });
        window.addActor(selectBox);


        final var changeButton = new TextButton("change", skin, "default");
        changeButton.setX(columnX);
        changeButton.setY(10);
        changeButton.setWidth(100);
        changeButton.setHeight(25);
        changeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//                lsd.getTint().setRed(Float.parseFloat(red.getText()));
//                lsd.getTint().setGreen(Float.parseFloat(green.getText()));
//                lsd.getTint().setBlue(Float.parseFloat(blue.getText()));
//                lsd.setType(LightSourceType.valueOf(selectBox.getSelected()));
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
