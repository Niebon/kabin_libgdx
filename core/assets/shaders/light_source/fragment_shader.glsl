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
uniform float[64] types;// CONE, SPHERE or BEAM (under corr float <-> int)
uniform vec2[64] light_sources;// Coordinates for light sources.
uniform vec3[64] light_tints;// Tints for light sources.
uniform float[64] radii;// Radii of light sources (CONE/SPHERE) or distance (BEAM)

uniform float[64] angles;// Light cone direction.
uniform float[64] widths;// Arc span - applicable to CONE (angular width) & BEAM - regular width.


uniform int number_of_sources;

float find_angle_deg(float dx, float dy);

void main() {
    vec4 new_color = ambient * v_color * texture2D(u_texture, v_texCoords);
    for (int i = 0; i < number_of_sources; i++) {
        float dx = light_sources[i].x - gl_FragCoord.x;
        float dy = light_sources[i].y - gl_FragCoord.y;
        float r = sqrt(dx * dx + dy * dy);
        float r0 = radii[i];
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
            case CONE: {
                float dx = light_sources[i].x - gl_FragCoord.x;
                float dy = light_sources[i].y - gl_FragCoord.y;
                float r = sqrt(dx * dx + dy * dy);

                float angle = degrees(atan(dy, dx));
                float arc_lower_bound = angles[i] - 0.5 * widths[i];
                float arc_upper_bound = angles[i] + 0.5 * widths[i];
                bool inside_cone = (arc_lower_bound <= angle && angle <= arc_upper_bound)
                ^^ (arc_lower_bound <= angle - 360 && angle - 360 <= arc_upper_bound)
                ^^ (arc_lower_bound <= angle + 360 && angle + 360 <= arc_upper_bound);

                if (inside_cone && r <= r0) {
                    float illum_factor = (r0 - r) / r0;
                    vec3 illum_factor_times_tint = illum_factor * light_tints[i];
                    vec4 final_color = vec4(illum_factor_times_tint, 1);
                    new_color = max(final_color * v_color * texture2D(u_texture, v_texCoords), new_color);
                }
                break;
            }
            case BEAM: {
                // TODO: implement
            }
            default : {
                break;
            }
        }
    }
    gl_FragColor = new_color;
}

float find_angle_deg(float dx, float dy) {

    float angle = atan(dy / dx);

    if (dx < 0 && dy >= 0) angle = 180 + angle;// 2nd quadrant
    else if (dx < 0 && dy <= 0) angle = 180 + angle;// 3nd quadrant
    else if (dx >= 0 && dy <= 0) angle = 360 + angle;// 4nd quadrant
    if (angle == 360) angle = 0;

    return angle;
}