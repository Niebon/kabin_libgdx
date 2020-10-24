package dev.kabin.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.kabin.utilities.helperinterfaces.FloatArea;
import dev.kabin.utilities.helperinterfaces.ModifiableFloatCoordinates;
import dev.kabin.utilities.helperinterfaces.Scalable;

public interface AnimationPlayer extends ModifiableFloatCoordinates, FloatArea, Scalable {

    void setCurrentAnimation(AnimationClass animationClass);

    void renderNextAnimationFrame(SpriteBatch batch, float stateTime);

    void renderFrameByIndex(SpriteBatch batch, int index);

    String getCurrentImageAssetPath();

    int getCurrentImageAssetIndex();
}
