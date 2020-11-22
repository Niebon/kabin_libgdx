package dev.kabin.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.utilities.helperinterfaces.FloatArea;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.utilities.helperinterfaces.Scalable;
import dev.kabin.utilities.pools.ImageAnalysisPool;

public interface AnimationPlayback extends ModifiableFloatCoordinates, FloatArea, Scalable {

    void setCurrentAnimation(AnimationClass animationClass);

    void renderNextAnimationFrame(SpriteBatch batch, float stateTime);

    void renderFrameByIndex(SpriteBatch batch, int index);

    String getCurrentImageAssetPath();

    int getCurrentImageAssetIndex();

    ImageAnalysisPool.Analysis getPixelAnalysis();
}
