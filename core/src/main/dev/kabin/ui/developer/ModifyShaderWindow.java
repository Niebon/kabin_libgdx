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

import java.util.Arrays;

class ModifyShaderWindow {

    public ModifyShaderWindow(Stage stage,
                              MouseEventUtil msu,
                              EntityLibgdx e) {

        final AnchoredLightSourceData lsd = e.getLightSourceData();

        final var skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));
        final var window = new Window("Actions", skin);
        final float width = 200;
        final float height = 200;
        window.setBounds(msu.getXRelativeToUI() + width * 0.1f, msu.getYRelativeToUI() + height * 0.1f,
                width, height);

        // Remove button.
        final var txtFieldX = new TextField(String.valueOf(lsd.getUnscaledXRelToAnchor()), skin, "default");
        final var txtFieldY = new TextField(String.valueOf(lsd.getUnscaledYRelToAnchor()), skin, "default");
        final var txtFieldR = new TextField(String.valueOf(lsd.getUnscaledR()), skin, "default");
        txtFieldX.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
        txtFieldY.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
        txtFieldR.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');

        Label lx = new Label("x:", skin);
        lx.setX(5);
        lx.setY(150);

        txtFieldX.setX(50);
        txtFieldX.setY(150);
        txtFieldX.setWidth(100);
        txtFieldX.setHeight(25);

        Label ly = new Label("y:", skin);
        ly.setX(5);
        ly.setY(115);
        txtFieldY.setX(50);
        txtFieldY.setY(115);
        txtFieldY.setWidth(100);
        txtFieldY.setHeight(25);

        Label lr = new Label("r:", skin);
        lr.setX(5);
        lr.setY(80);
        txtFieldR.setX(50);
        txtFieldR.setY(80);
        txtFieldR.setWidth(100);
        txtFieldR.setHeight(25);

        window.addActor(lx);
        window.addActor(ly);
        window.addActor(lr);
        window.addActor(txtFieldR);
        window.addActor(txtFieldX);
        window.addActor(txtFieldY);


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

}
