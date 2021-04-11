package dev.kabin.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderFactory {

    public static ShaderProgram lightSourceShader() {
        return new ShaderProgram("""
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
                """,
                """
                        #ifdef GL_ES
                        precision mediump float;
                        #endif
                                        
                        varying vec4 v_color;
                        varying vec2 v_texCoords;
                        varying vec2 gl_FragCoord;
                          
                        uniform vec2 light_source;
                        uniform float r02;
                        uniform sampler2D u_texture;
                                        
                        void main() {
                          float dx = light_source.x - gl_FragCoord.x;
                          float dy = light_source.y - gl_FragCoord.y;
                          float r2 = sqrt(dx * dx + dy * dy);
                          if (r2 <= r02) {
                            float f = r2 / r02;
                            float level = 0.1 + (1 - f) * 0.9;
                            vec4 ambient = vec4(level, level, level, 1);
                            gl_FragColor = ambient * v_color * texture2D(u_texture, v_texCoords);
                          }
                          else {
                            gl_FragColor = vec4(0.1, 0.1, 0.1, 1) * v_color * texture2D(u_texture, v_texCoords);
                          }
                        }
                        """);
    }

    public static ShaderProgram ambientShader() {
        return new ShaderProgram("""
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
                """, """
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
                """);
    }
}
