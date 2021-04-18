package dev.kabin.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderFactory {

    public static ShaderProgram lightSourceShader() {
        return new ShaderProgram(
                Gdx.files.internal("shaders/light_source/vertex_shader.glsl"),
                Gdx.files.internal("shaders/light_source/fragment_shader.glsl")
        );
    }

    public static ShaderProgram ambientShader() {
        return new ShaderProgram(
                Gdx.files.internal("shaders/ambient/vertex_shader.glsl"),
                Gdx.files.internal("shaders/ambient/fragment_shader.glsl")
        );
    }


}
