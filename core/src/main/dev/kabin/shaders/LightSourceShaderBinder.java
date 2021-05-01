package dev.kabin.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.function.IntFunction;

public class LightSourceShaderBinder {

    private static final int NUMBER_OF_PARTICLES = 64;

    private final ShaderProgram prg;
    private final float[] types = new float[NUMBER_OF_PARTICLES];
    private final float[] tints = new float[NUMBER_OF_PARTICLES * 3];
    private final float[] xy = new float[NUMBER_OF_PARTICLES * 2];
    private final float[] r = new float[NUMBER_OF_PARTICLES];
    private final float[] angles = new float[NUMBER_OF_PARTICLES];
    private final float[] widths = new float[NUMBER_OF_PARTICLES];

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
        prg.setUniformf("ambient", red, green, blue, alpha);
    }


    /**
     * A procedure for associating data to the <i>i</i>'th light source, where <i>i</i> ranges from 0 to the given number of light sources.
     *
     * @param numberLightSources the number of light sources to iterate over by index, starting from zero.
     */
    public void bindData(IntFunction<LightSourceData> lightSourceDataIntFunction,
                         float camXMinusHalfWidth,
                         float camYMinusHalfHeight,
                         int numberLightSources) {

        for (int i = 0; i < numberLightSources; i++) {

            this.types[i] = lightSourceDataIntFunction.apply(i).getType().getFloatValue();

            this.tints[3 * i] = lightSourceDataIntFunction.apply(i).getTint().red();
            this.tints[3 * i + 1] = lightSourceDataIntFunction.apply(i).getTint().green();
            this.tints[3 * i + 2] = lightSourceDataIntFunction.apply(i).getTint().blue();

            this.xy[2 * i] = lightSourceDataIntFunction.apply(i).getX() - camXMinusHalfWidth;
            this.xy[2 * i + 1] = lightSourceDataIntFunction.apply(i).getY() - camYMinusHalfHeight;

            this.r[i] = lightSourceDataIntFunction.apply(i).getR();

            this.angles[i] = lightSourceDataIntFunction.apply(i).getAngle();
            this.widths[i] = lightSourceDataIntFunction.apply(i).getWidth();
        }

        prg.setUniform2fv("light_sources", xy, 0, xy.length);
        prg.setUniform3fv("light_tints", this.tints, 0, this.tints.length);
        prg.setUniform1fv("types", this.types, 0, this.types.length);
        prg.setUniform1fv("radii", this.r, 0, this.r.length);
        prg.setUniformi("number_of_sources", numberLightSources);
        prg.setUniform1fv("angles", angles, 0, this.angles.length);
        prg.setUniform1fv("widths", widths, 0, this.widths.length);

    }

}
