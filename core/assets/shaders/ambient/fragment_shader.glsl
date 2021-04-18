#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 gl_FragCoord;

uniform sampler2D u_texture;

void main() {
    gl_FragColor = vec4(0.1, 0.1, 0.1, 1) * v_color * texture2D(u_texture, v_texCoords);
}