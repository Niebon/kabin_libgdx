package dev.kabin.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.kabin.util.functioninterfaces.IntToFloatFunction;

import java.util.function.IntFunction;

public class LightSourceShaderBinder {

    private static final int NUMBER_OF_PARTICLES = 64;

    private final ShaderProgram prg;
    private final float[] types = new float[NUMBER_OF_PARTICLES];
    private final float[] tints = new float[NUMBER_OF_PARTICLES * 3];
    private final float[] xy = new float[NUMBER_OF_PARTICLES * 2];
    private final float[] r = new float[NUMBER_OF_PARTICLES];
    private final AmbientColor ambient = new AmbientColor();

    public LightSourceShaderBinder(ShaderProgram lightSourceShaderProgram) {
        this.prg = lightSourceShaderProgram;
    }

    /**
     * Sets the ambient color to the given colors. Levels <b>should</b> lie in the range [0,1].
     *
     * @param red   red level.
     * @param green green level.
     * @param blue  blue level.
     * @param alpha alpha level.
     */
    public void setAmbient(float red, float green, float blue, float alpha) {
        ambient.red = red;
        ambient.green = green;
        ambient.blue = blue;
        ambient.alpha = alpha;
    }


    /**
     * A procedure for associating data to the <i>i</i>'th light source, where <i>i</i> ranges from 0 to the given number of light sources.
     *
     * @param types              the type.
     * @param tints              the tint.
     * @param x                  the horizontal coordinate.
     * @param y                  the vertical coordinate.
     * @param r                  the radius.
     * @param numberLightSources the number of light sources to iterate over by index, starting from zero.
     */
    public void bindData(IntFunction<LightSourceType> types,
                         IntFunction<Tint> tints,
                         IntToFloatFunction x,
                         IntToFloatFunction y,
                         IntToFloatFunction r,
                         int numberLightSources) {

        for (int i = 0; i < numberLightSources; i++) {

            this.types[i] = types.apply(i).getFloatValue();

            this.tints[3 * i] = tints.apply(i).red();
            this.tints[3 * i + 1] = tints.apply(i).green();
            this.tints[3 * i + 2] = tints.apply(i).blue();

            this.xy[2 * i] = x.apply(i);
            this.xy[2 * i + 1] = y.apply(i);

            this.r[i] = r.apply(i);

        }

        prg.setUniform2fv("light_sources", xy, 0, xy.length);
        prg.setUniform3fv("light_tints", this.tints, 0, this.tints.length);
        prg.setUniform1fv("types", this.types, 0, this.types.length);
        prg.setUniform1fv("radii", this.r, 0, this.r.length);
        prg.setUniformi("number_of_sources", numberLightSources);
        prg.setUniformf("ambient", ambient.red, ambient.green, ambient.blue, ambient.alpha);
    }

    /**
     * Wrapper class for the ambient color.
     */
    private static final class AmbientColor {
        private float red;
        private float green;
        private float blue;
        private float alpha;
    }
}
