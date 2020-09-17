package dev.kabin.graphics;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationBundleFactory {

    public static String ASSETS_RAW_TEXTURES = "core/assets/raw_textures/";

    public static AnimationBundle load(String assetPath) {

        final File directory = new File(ASSETS_RAW_TEXTURES + assetPath);
        if (!directory.isDirectory()) throw new IllegalArgumentException("The provided assetPath '" + assetPath
                + "'did not correspond to a directory.");
        final Map<Animations.AnimationType, List<Integer>> animationTypeToIntListMap
                = new EnumMap<>(Animations.AnimationType.class);
        for (Animations.AnimationType type : Animations.AnimationType.values()) {
            final File[] filesStartingWithTypeName = Arrays.stream(directory.listFiles())
                    .filter(f -> f.getName().startsWith(type.name()))
                    .toArray(File[]::new);
            if (filesStartingWithTypeName.length > 0) {
                animationTypeToIntListMap.put(type,

                        Arrays.stream(filesStartingWithTypeName)
                                .map(File::getName)
                                .map(s -> s.replace(type.name() + "_", ""))
                                .map(s -> s.replace(".png", ""))
                                .map(Integer::valueOf)
                                .collect(Collectors.toList())
                );
            }
        }

        return new AnimationBundle(assetPath, animationTypeToIntListMap);
    }


}
