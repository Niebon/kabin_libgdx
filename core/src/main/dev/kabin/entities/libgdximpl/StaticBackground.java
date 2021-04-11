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
                    params.camX() - 0.5f * params.screenWidth(),
                    params.camY() - 0.5f * params.screenHeight()
            );
            animationPlaybackImpl.setShaderProgram(params.shaderFor(getGroupType()));
            animationPlaybackImpl.renderNextAnimationFrame(params);
            animationPlaybackImpl.setScale(params.scale());
        }
    }

    public static StaticBackground getInstance() {
        return instance;
    }

}
