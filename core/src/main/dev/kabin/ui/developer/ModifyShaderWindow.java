package dev.kabin.ui.developer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.shaders.AnchoredLightSourceData;
import dev.kabin.shaders.LightSourceType;
import dev.kabin.util.eventhandlers.MouseEventUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

class ModifyShaderWindow {

    ModifyShaderWindow(Stage stage,
                       MouseEventUtil msu,
                       EntityLibgdx e) {

        final AnchoredLightSourceData lsd = e.getLightSourceData();

        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var window = new Window("Actions", skin);
        final float width = 250;
        final float height = 300;
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f,
                width, height);

        final var sliderR = new Slider(0f, 264, 1.0f, false, skin);
        sliderR.setX(50);
        sliderR.setY(185);
        sliderR.setWidth(100);
        sliderR.setHeight(25);
        window.addActor(sliderR);


        final TextField txtFieldX = makeTextField(50, 150, 100, 25,
                String.valueOf(lsd.getUnscaledXRelToAnchor()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(txtFieldX);

        final TextField txtFieldY = makeTextField(50, 115, 100, 25,
                String.valueOf(lsd.getUnscaledXRelToAnchor()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(txtFieldY);

        final TextField txtFieldR = makeTextField(50, 80, 100, 25,
                String.valueOf(lsd.getR()),
                (textField, c) -> Character.isDigit(c) || c == '.',
                skin);
        window.addActor(txtFieldR);


        Label lx = new Label("x:", skin);
        lx.setX(5);
        lx.setY(150);
        window.addActor(lx);


        Label ly = new Label("y:", skin);
        ly.setX(5);
        ly.setY(115);
        window.addActor(ly);


        Label lr = new Label("r:", skin);
        lr.setX(5);
        lr.setY(80);
        window.addActor(lr);


        final var selectBox = new SelectBox<String>(skin, "default");
        selectBox.setItems(
                Arrays.stream(LightSourceType.values())
                        .map(Enum::name)
                        .toArray(String[]::new)
        );
        selectBox.setSelectedIndex(lsd.getType().ordinal());
        selectBox.setWidth(100);
        selectBox.setHeight(25);
        selectBox.setX(50);
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
        changeButton.setX(50);
        changeButton.setY(10);
        changeButton.setWidth(100);
        changeButton.setHeight(25);
        changeButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lsd.setXRelToAnchor(Float.parseFloat(txtFieldX.getText()) * lsd.getScale());
                lsd.setYRelToAnchor(Float.parseFloat(txtFieldY.getText()) * lsd.getScale());
                lsd.setR(Float.parseFloat(txtFieldR.getText()) * lsd.getScale());
                lsd.setType(LightSourceType.valueOf(selectBox.getSelected()));
                window.remove();
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

}
