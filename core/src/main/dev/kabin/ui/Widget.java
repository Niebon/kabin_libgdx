package dev.kabin.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.kabin.utilities.Functions;

import java.util.Arrays;
import java.util.Objects;


public class Widget {

    private final Group backingGroup = new Group();
    private final Dialog dialog;

    private Widget(
            Skin skin,
            String title,
            float x,
            float y,
            float width,
            float height
    ) {
        dialog = new Dialog(title, skin);
        dialog.setBounds(x, y, width, height);
        backingGroup.addActor(dialog);
    }

    public void addDialogActor(Actor... a) {
        dialog.add(a);
    }

    public void addActor(Actor a) {
        backingGroup.addActor(a);
    }

    public void removeActor(Actor a) {
        backingGroup.removeActor(a);
    }

    public boolean isDragging() {
        return dialog.isDragging() || Arrays.stream(backingGroup.getChildren().toArray())
                .filter(c -> c instanceof Dialog)
                .map(c -> (Dialog) c)
                .anyMatch(Dialog::isDragging);
    }

    public static class Builder {

        private static final Skin DEFAULT_SKIN = new Skin(Gdx.files.internal("uiskin.json"));
        private static final String DEFAULT_TITLE = "default title";
        private static final int DEFAULT_WIDTH = 600;
        private static final int DEFAULT_HEIGHT = 200;


        private Skin skin;
        private String title;
        private Dialog dialog;
        private float width, height, x, y;

        public Widget build(){
            return new Widget(
                    Objects.requireNonNullElse(skin, DEFAULT_SKIN),
                    Objects.requireNonNullElse(title, DEFAULT_TITLE),
                    x, y,
                    Functions.requireNonNullElse(width, DEFAULT_WIDTH),
                    Functions.requireNonNullElse(height, DEFAULT_HEIGHT)
            );
        }
    }

}
