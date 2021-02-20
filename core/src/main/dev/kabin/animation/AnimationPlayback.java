package dev.kabin.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.util.helperinterfaces.FloatArea;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.Scalable;
import dev.kabin.util.pools.ImageAnalysisPool;

public interface AnimationPlayback extends ModifiableFloatCoordinates, FloatArea, Scalable {

    void setCurrentAnimation(AnimationClass animationClass);

    void renderNextAnimationFrame(SpriteBatch batch, float stateTime);

    void renderFrameByIndex(SpriteBatch batch, int index);

    String getCurrentImageAssetPath();

    int getCurrentImageAssetIndex();

    ImageAnalysisPool.Analysis getPixelAnalysis();

    void reset();

    void setSmoothParameters(float alpha, float beta);
}
