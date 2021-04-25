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
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f,
                width, height);


        final int columnX = 60;

        final var sliderX = makeSlider(60, 255, 100, 25,
                -128, 128, 1.0f, false, skin, lsd.getUnscaledXRelToAnchor(), lsd::setXRelToAnchor);
        window.addActor(sliderX);

        final var sliderY = makeSlider(60, 220, 100, 25,
                -128, 128, 1.0f, false, skin, lsd.getUnscaledYRelToAnchor(), lsd::setYRelToAnchor);
        window.addActor(sliderY);

        final var sliderR = makeSlider(60, 185, 100, 25,
                0f, 264, 1.0f, false, skin, lsd.getR(), lsd::setR);
        window.addActor(sliderR);


        final TextField red = makeTextField(columnX, 150, 100, 25,
                String.valueOf(lsd.getTint().red()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(red);

        final TextField green = makeTextField(columnX, 115, 100, 25,
                String.valueOf(lsd.getTint().green()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(green);

        final TextField blue = makeTextField(columnX, 80, 100, 25,
                String.valueOf(lsd.getTint().blue()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(blue);


        Label lx = new Label("x:", skin);
        lx.setX(5);
        lx.setY(255);
        window.addActor(lx);


        Label ly = new Label("y:", skin);
        ly.setX(5);
        ly.setY(220);
        window.addActor(ly);


        Label lr = new Label("r:", skin);
        lr.setX(5);
        lr.setY(185);
        window.addActor(lr);


        Label lred = new Label("red:", skin);
        lred.setX(5);
        lred.setY(150);
        window.addActor(lred);


        Label lgreen = new Label("green:", skin);
        lgreen.setX(5);
        lgreen.setY(115);
        window.addActor(lgreen);


        Label lblue = new Label("blue:", skin);
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
//                lsd.setXRelToAnchor(Float.parseFloat(red.getText()) * lsd.getScale());
//                lsd.setYRelToAnchor(Float.parseFloat(green.getText()) * lsd.getScale());
//                lsd.setR(Float.parseFloat(blue.getText()) * lsd.getScale());
                lsd.getTint().setRed(Float.parseFloat(red.getText()));
                lsd.getTint().setGreen(Float.parseFloat(green.getText()));
                lsd.getTint().setBlue(Float.parseFloat(blue.getText()));
                lsd.setType(LightSourceType.valueOf(selectBox.getSelected()));
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
            String initialText,
            TextField.TextFieldFilter textFieldFilter,
            Skin skin) {
        final var txtFieldX = new TextField(initialText, skin, "default");
        txtFieldX.setTextFieldFilter(textFieldFilter);
        txtFieldX.setX(x);
        txtFieldX.setY(y);
        txtFieldX.setWidth(width);
        txtFieldX.setHeight(height);
        return txtFieldX;
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
            Skin skin,
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
