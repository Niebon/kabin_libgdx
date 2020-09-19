package dev.kabin.graphics.animation;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import dev.kabin.global.GlobalData;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnimationBundleFactory {

    public static String ASSETS_RAW_TEXTURES = "core/assets/raw_textures/";

    @Deprecated
    @SuppressWarnings("unused")
    public static AnimationBundle loadFromAssetPath(String assetPath) {

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

        final Array<TextureAtlas.AtlasRegion> animations = findAllAnimations(assetPath);

        return new AnimationBundle(animations, animationTypeToIntListMap);
    }

    private static Map<Animations.AnimationType, List<Integer>> findAnimationTypeToIntListMap(String atlasPath) {
        return Arrays.stream(Animations.AnimationType.values())
                .filter(type -> GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).size > 0)
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> Arrays.stream(GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).toArray())
                                .map(reg -> reg.index)
                                .collect(Collectors.toList())
                ));
    }

    private static Array<TextureAtlas.AtlasRegion> findAllAnimations(String atlasRegionPath) {
        return new Array<>(Arrays.stream(Animations.AnimationType.values())
                .map(type -> GlobalData.getAtlas().findRegions(atlasRegionPath + '/' + type.name()))
                .filter(Objects::nonNull)
                .filter(Array::notEmpty)
                .sorted(Comparator.comparing(region -> region.first().index))
                .map(Array::toArray)
                .flatMap(Stream::of)
                .toArray(TextureAtlas.AtlasRegion[]::new));
    }

    public static AnimationBundle loadFromAtlasPath(String atlasPath) {
        final Map<Animations.AnimationType, List<Integer>> animationTypeToIntListMap = findAnimationTypeToIntListMap(atlasPath);
        final Array<TextureAtlas.AtlasRegion> animations = findAllAnimations(atlasPath);
        return new AnimationBundle(animations, animationTypeToIntListMap);
    }


}