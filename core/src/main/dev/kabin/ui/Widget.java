package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kabin.global.GlobalData;
import dev.kabin.utilities.Functions;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;

import java.util.Arrays;
import java.util.Objects;


public class Widget implements ModifiableFloatCoordinates {

    private final Group backingGroup = new Group();
    private final Window mainWindow;
    private final Window collapsedWindow;
    private Window[] popupWindows;
    private Label contentTableMessage;
    private boolean visible, collapsed;

    private Widget(
            float x, float y, float width, float height, String title, Skin skin,
            float collapsedWindowX, float collapsedWindowY, float collapsedWindowWidth, float collapsedWindowHeight,
            Label contentTableMessage
    ) {
        collapsedWindow = new Window(title, skin);
        collapsedWindow.setBounds(collapsedWindowX, collapsedWindowY, collapsedWindowWidth, collapsedWindowHeight);
        collapsedWindow.setMovable(false);


        mainWindow = new Window(title, skin);
        mainWindow.setBounds(x, y, width, height);
        backingGroup.addActor(mainWindow);
        refreshContentTableMessage(contentTableMessage);


        var collapseButton = new TextButton("_", Widget.Builder.DEFAULT_SKIN, "default");
        collapseButton.setWidth(20);
        collapseButton.setHeight(20);
        collapseButton.padRight(0);
        collapseButton.padTop(0);
        mainWindow.getTitleTable().add(collapseButton);


        collapseButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mainWindow.remove();
                backingGroup.addActor(collapsedWindow);
                collapsed = true;
                return true;
            }
        });

        // Need this substitution to reference values from constructor parameters.
        float mainWindowX = x, mainWindowY = y;
        collapsedWindow.addListener(new ClickListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                collapsedWindow.remove();
                backingGroup.addActor(mainWindow);
                mainWindow.setBounds(mainWindowX, mainWindowY, width, height);
                collapsed = false;
                return true;
            }
        });
    }


    public void setVisible(boolean b) {
        visible = b;
        if (b) {
            GlobalData.stage.addActor(backingGroup);
        } else {
            backingGroup.remove();
        }
    }

    public boolean isCollapsed(){
        return collapsed;
    }

    public boolean isVisible(){
        return visible;
    }

    public void addDialogActor(Actor a) {
        mainWindow.addActor(a);
    }

    public void refreshContentTableMessage(Label contentTableMessage) {
        if (contentTableMessage != null) {
            mainWindow.removeActor(this.contentTableMessage);
            mainWindow.addActor(this.contentTableMessage = contentTableMessage);
        }
    }

    public void removeDialogActor(Actor a) {
        mainWindow.removeActor(a);
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
        popupWindows = Arrays.stream(backingGroup.getChildren().toArray())
                .filter(c -> c instanceof Window)
                .map(c -> (Window) c)
                .toArray(Window[]::new);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDragging() {
        return mainWindow.isDragging() ||
                collapsedWindow.isDragging() ||
                (popupWindows != null && Functions.anyTrue(popupWindows, Window::isDragging));
    }

    @Override
    public float getX() {
        return mainWindow.getX();
    }

    @Override
    public void setX(float x) {
        mainWindow.setX(x);
    }

    @Override
    public float getY() {
        return mainWindow.getY();
    }

    @Override
    public void setY(float y) {
        mainWindow.setY(y);
    }


    public static class Builder {

        public static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal("uiskin.json"));
        private static final String DEFAULT_TITLE = "default title";
        private static final int DEFAULT_WIDTH = 600;
        private static final int DEFAULT_HEIGHT = 200;

        private Skin skin;
        private String title;
        private float width, height, x, y;
        private float collapsedWindowWidth, collapsedWindowHeight, collapsedWindowX, collapsedWindowY;
        private Label contentTableMessage;

        @SuppressWarnings("unused")
        public Builder setSkin(Skin skin) {
            this.skin = skin;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCollapsedWindowWidth(float collapsedWindowWidth) {
            this.collapsedWindowWidth = collapsedWindowWidth;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCollapsedWindowHeight(float collapsedWindowHeight) {
            this.collapsedWindowHeight = collapsedWindowHeight;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCollapsedWindowX(float collapsedWindowX) {
            this.collapsedWindowX = collapsedWindowX;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCollapsedWindowY(float collapsedWindowY) {
            this.collapsedWindowY = collapsedWindowY;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setContentTableMessage(Label contentTableMessage) {
            this.contentTableMessage = contentTableMessage;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setX(float x) {
            this.x = x;
            return this;
        }

        @SuppressWarnings("unused")
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
                    Functions.requireNonNullElse(collapsedWindowX, x),
                    Functions.requireNonNullElse(collapsedWindowY, y),
                    Functions.requireNonNullElse(collapsedWindowWidth, 20),
                    Functions.requireNonNullElse(collapsedWindowHeight, 20),
                    contentTableMessage
            );
        }
    }


}
