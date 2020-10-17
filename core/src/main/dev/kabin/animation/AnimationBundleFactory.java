package dev.kabin.animation;

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
        final Map<Animations.AnimationType, int[]> animationTypeToIntListMap
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
                                .mapToInt(Integer::valueOf)
                                .toArray()
                );
            }
        }

        final Array<TextureAtlas.AtlasRegion> animations = findAllAnimations(assetPath, Animations.AnimationType.values());

        return new AnimationBundle(animations, animationTypeToIntListMap);
    }

    public static <T extends Enum<T>> Map<T, int[]> findEnumTypeToIntArrayMapping(String atlasPath, T[] values) {
        return Arrays.stream(values)
                .filter(type -> GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).size > 0)
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> Arrays.stream(GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).toArray())
                                .mapToInt(reg -> reg.index)
                                .toArray()
                ));
    }

    public static <T extends Enum<T>> Map<T, TextureAtlas.AtlasRegion[]> findTypeToAtlasRegionsMapping(String atlasRegionPath,
                                                                                                       T[] values) {
        return Arrays.stream(values).map(
                type -> new AbstractMap.SimpleEntry<>(
                        type,
                        GlobalData.getAtlas().findRegions(atlasRegionPath + '/' + type.name())
                ))
                .filter(e -> e.getValue() != null && e.getValue().notEmpty())
                .sorted(Comparator.comparing(e -> e.getValue().first().index))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().toArray()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public static <T extends Enum<T>> Array<TextureAtlas.AtlasRegion> findAllAnimations(String atlasRegionPath, T[] values) {
        return new Array<>(Arrays.stream(values)
                .map(type -> GlobalData.getAtlas().findRegions(atlasRegionPath + '/' + type.name()))
                .filter(Objects::nonNull)
                .filter(Array::notEmpty)
                .sorted(Comparator.comparing(region -> region.first().index))
                .map(Array::toArray)
                .flatMap(Stream::of)
                .toArray(TextureAtlas.AtlasRegion[]::new));
    }

    public static AnimationBundle loadFromAtlasPath(String atlasPath) {
        final Map<Animations.AnimationType, int[]> animationTypeToIntListMap = findEnumTypeToIntArrayMapping(atlasPath, Animations.AnimationType.values());
        final Array<TextureAtlas.AtlasRegion> animations = findAllAnimations(atlasPath, Animations.AnimationType.values());
        return new AnimationBundle(animations, animationTypeToIntListMap);
    }


}
