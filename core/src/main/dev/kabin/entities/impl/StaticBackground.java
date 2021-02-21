package dev.kabin.entities.impl;

import dev.kabin.entities.GraphicsParameters;

public class StaticBackground extends EntitySimple {

    private static StaticBackground instance;

    StaticBackground(EntityParameters parameters) {
        super(parameters);
        instance = this;
        animationPlaybackImpl.setSmoothParameters(1f, 0, 0);
    }

    @Override
    public boolean touchDown(int button) {
        return false;
    }

    @Override
    public void updateGraphics(GraphicsParameters params) {
        animationPlaybackImpl.setPos(
                params.getCamX() - 0.5f * params.getScreenWidth(),
                params.getCamY() - 0.5f * params.getScreenHeight()
        );
        animationPlaybackImpl.renderNextAnimationFrame(params);
        animationPlaybackImpl.setScale(params.getScale());
    }

    public static StaticBackground getInstance() {
        return instance;
    }

    @Override
    public EntityFactory.EntityType getType() {
        return EntityFactory.EntityType.STATIC_BACKGROUND;
    }

}
