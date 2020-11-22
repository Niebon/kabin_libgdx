package dev.kabin;

import dev.kabin.ui.DeveloperUI;

public class MainGameDeveloper extends MainGame {

    @Override
    public void create() {
        super.create();
        DeveloperUI.loadWorld();
    }
}
