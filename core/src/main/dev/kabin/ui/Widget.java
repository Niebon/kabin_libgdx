package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;

import java.util.Arrays;
import java.util.Objects;


public class Widget implements ModifiableFloatCoordinates {

    private final Group backingGroup = new Group();
    private final Window window;
    private Window[] windowsInBackingGroupCached;
    private Label contentTableMessage;

    private Widget(
            float x, float y, float width, float height, String title, Skin skin,
            Label contentTableMessage
    ) {
        window = new Window(title, skin);
        window.setBounds(x, y, width, height);
        backingGroup.addActor(window);
        refreshContentTableMessage(contentTableMessage);


        var collapseButton = new TextButton("x", Widget.Builder.DEFAULT_SKIN, "default");

    }

    public void setVisible(boolean b) {
        if (b) {
            GlobalData.stage.addActor(backingGroup);
        } else {
            backingGroup.remove();
        }
    }

    public void addDialogActor(Actor a) {
        window.addActor(a);
    }

    public void refreshContentTableMessage(Label contentTableMessage) {
        if (contentTableMessage != null) {
            window.removeActor(this.contentTableMessage);
            window.addActor(this.contentTableMessage = contentTableMessage);
        }
    }

    public void removeDialogActor(Actor a) {
        window.removeActor(a);
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
        windowsInBackingGroupCached = Arrays.stream(backingGroup.getChildren().toArray())
                .filter(c -> c instanceof Window)
                .map(c -> (Window) c)
                .toArray(Window[]::new);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDragging() {
        return window.isDragging() || (windowsInBackingGroupCached != null && Functions.anyTrue(windowsInBackingGroupCached, Window::isDragging));
    }

    @Override
    public float getX() {
        return window.getX();
    }

    @Override
    public void setX(float x) {
        window.setX(x);
    }

    @Override
    public float getY() {
        return window.getY();
    }

    @Override
    public void setY(float y) {
        window.setY(y);
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
