package dev.kabin.entities.libgdximpl;

import java.util.Optional;

public class StaticBackground extends AbstractLibgdxEntity {

    private static StaticBackground instance;

    StaticBackground(EntityParameters parameters) {
        super(parameters);
        instance = this;
        Optional.ofNullable(getAnimationPlaybackImpl()).ifPresent(a -> a.setSmoothParameter(1f));
        actor().remove();
    }

    @Override
    public void updateGraphics(GraphicsParametersLibgdx params) {
        final var animationPlaybackImpl = getAnimationPlaybackImpl();
        if (animationPlaybackImpl != null) {
            animationPlaybackImpl.setPos(
                    params.getCamX() - 0.5f * params.getScreenWidth(),
                    params.getCamY() - 0.5f * params.getScreenHeight()
            );
            animationPlaybackImpl.renderNextAnimationFrame(params);
            animationPlaybackImpl.setScale(params.getScale());
        }
    }

    public static StaticBackground getInstance() {
        return instance;
    }

}
