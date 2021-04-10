package dev.kabin.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class LightSourceShader {

    private static final String VERTEX_SHADER = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
               v_color = vec4(1, 1, 1, 1);
               v_texCoords = a_texCoord0;
               gl_Position =  u_projTrans * a_position;
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            void main() {
              gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
            }
            """;

    public static ShaderProgram make() {
        return new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    }
}
