package dev.kabin.utilities.pools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

public class FontPool {

    public static final String juicyPixelFontPath = "fonts/juicy_pixel_font.ttf";
    private static final Map<Integer, BitmapFont> fontMap = new HashMap<>();

    public static BitmapFont find(int size) {
        if (!fontMap.containsKey(size)) {
            final FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(juicyPixelFontPath));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            BitmapFont font = gen.generateFont(parameter);
            fontMap.put(size, font);
            gen.dispose();
        }
        return fontMap.get(size);
    }

}
