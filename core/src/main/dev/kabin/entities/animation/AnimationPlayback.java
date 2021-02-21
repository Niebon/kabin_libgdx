package dev.kabin.entities.animation;

import dev.kabin.entities.GraphicsParameters;
import dev.kabin.util.helperinterfaces.FloatArea;
import dev.kabin.util.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.util.helperinterfaces.Scalable;
import dev.kabin.util.pools.ImageAnalysisPool;

public interface AnimationPlayback extends ModifiableFloatCoordinates, FloatArea, Scalable {

    void setCurrentAnimation(AnimationClass animationClass);

    void renderNextAnimationFrame(GraphicsParameters params);

    void renderFrameByIndex(GraphicsParameters params, int index);

    String getCurrentImageAssetPath();

    int getCurrentImageAssetIndex();

    ImageAnalysisPool.Analysis getPixelAnalysis();

    void reset();

    void setSmoothParameters(float alpha, float initX, float initY);
}
