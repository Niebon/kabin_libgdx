package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;

import java.util.Arrays;
import java.util.Objects;


public class Widget implements ModifiableFloatCoordinates {

    private final Group backingGroup = new Group();
    private final Dialog dialog;
    private Dialog[] dialogsInGroup;
    private Label contentTableMessage;

    private Widget(
            float x, float y, float width, float height, String title, Skin skin,
            Label contentTableMessage
    ) {
        dialog = new Dialog(title, skin);
        dialog.setBounds(x, y, width, height);
        backingGroup.addActor(dialog);
        refreshContentTableMessage(contentTableMessage);
    }

    public void setVisible(boolean b) {
        if (b) {
            GlobalData.stage.addActor(backingGroup);
        } else {
            backingGroup.remove();
        }
    }

    public void addDialogActor(Actor a) {
        dialog.addActor(a);
    }

    public void refreshContentTableMessage(Label contentTableMessage) {
        dialog.getContentTable().clear();
        dialog.getContentTable().defaults();
        dialog.removeActor(this.contentTableMessage);
        dialog.addActor(this.contentTableMessage = contentTableMessage);
    }

    public void removeDialogActor(Actor a) {
        dialog.removeActor(a);
    }


    public void addActor(Actor a) {
        backingGroup.addActor(a);
        updateDialogsInGroup();
    }

    public void removeActor(Actor a) {
        backingGroup.removeActor(a);
        updateDialogsInGroup();
    }

    private void updateDialogsInGroup() {
        dialogsInGroup = Arrays.stream(backingGroup.getChildren().toArray())
                .filter(c -> c instanceof Dialog)
                .map(c -> (Dialog) c)
                .toArray(Dialog[]::new);
    }

    public boolean isDragging() {
        return dialog.isDragging() || (dialogsInGroup != null && Functions.anyTrue(dialogsInGroup, Dialog::isDragging));
    }

    @Override
    public float getX() {
        return dialog.getX();
    }

    @Override
    public void setX(float x) {
        dialog.setX(x);
    }

    @Override
    public float getY() {
        return dialog.getY();
    }

    @Override
    public void setY(float y) {
        dialog.setY(y);
    }


    public static class Builder {

        public static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal("uiskin.json"));
        private static final String DEFAULT_TITLE = "default title";
        private static final int DEFAULT_WIDTH = 600;
        private static final int DEFAULT_HEIGHT = 200;

        private Skin skin;
        private String title;
        private float width, height, x, y;
        private Label contentTableMessage;

        public Builder setSkin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public Builder setContentTableMessage(Label contentTableMessage) {
            this.contentTableMessage = contentTableMessage;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        public Builder setY(float y) {
            this.y = y;
            return this;
        }

        public Widget build() {
            return new Widget(
                    x, y,
                    Functions.requireNonNullElse(width, DEFAULT_WIDTH),
                    Functions.requireNonNullElse(height, DEFAULT_HEIGHT),
                    Objects.requireNonNullElse(title, DEFAULT_TITLE),
                    Objects.requireNonNullElse(skin, DEFAULT_SKIN),
                    contentTableMessage
            );
        }
    }


}
