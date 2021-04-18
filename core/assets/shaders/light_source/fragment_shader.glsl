#version 330
#ifdef GL_20
precision mediump float;
#endif

// Constants:
const int CONE = 10;
const int SPHERE = 20;
const int BEAM = 30;

// Varying:
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 gl_FragCoord;

// Uniform:
uniform sampler2D u_texture;
uniform vec4 ambient;
uniform float[64] types;
uniform vec2[64] light_sources;
uniform vec3[64] light_tints;
uniform float[64] radii;
uniform int number_of_sources;

void main() {
    vec4 new_color = ambient * v_color * texture2D(u_texture, v_texCoords);
    for (int i = 0; i < number_of_sources; i++) {
        float dx = light_sources[i].x - gl_FragCoord.x;
        float dy = light_sources[i].y - gl_FragCoord.y;
        float r = sqrt(dx * dx + dy * dy);
        float r0 = radii[i];
        if (r <= r0) {
            float illum_factor = (r0 - r) / r0;
            vec3 illum_factor_times_tint = illum_factor * light_tints[i];
            vec4 final_color = vec4(illum_factor_times_tint, 1);
            new_color = max(final_color * v_color * texture2D(u_texture, v_texCoords), new_color);
        }
        switch (floatBitsToInt(types[i])) {
            case SPHERE: {
                float dx = light_sources[i].x - gl_FragCoord.x;
                float dy = light_sources[i].y - gl_FragCoord.y;
                float r = sqrt(dx * dx + dy * dy);
                if (r <= r0) {
                    float illum_factor = (r0 - r) / r0;
                    vec3 illum_factor_times_tint = illum_factor * light_tints[i];
                    vec4 final_color = vec4(illum_factor_times_tint, 1);
                    new_color = max(final_color * v_color * texture2D(u_texture, v_texCoords), new_color);
                }
                break;
            }
            default : {
                break;
            }
        }
    }
    gl_FragColor = new_color;
}