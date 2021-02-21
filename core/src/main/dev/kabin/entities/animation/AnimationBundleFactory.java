package dev.kabin.entities.animation;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import dev.kabin.GlobalData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnimationBundleFactory {

    public static Map<Enum<?>, int[]> findEnumTypeToIntArrayMapping(String atlasPath, Class<?> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
                .map(e -> (Enum<?>) e)
                .filter(type -> GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).size > 0)
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> Arrays.stream(GlobalData.getAtlas().findRegions(atlasPath + '/' + type.name()).toArray())
                                .mapToInt(reg -> reg.index)
                                .toArray()
                ));
    }

    public static <T extends Enum<T>> Map<T, TextureAtlas.AtlasRegion[]> findTypeToAtlasRegionsMapping(String atlasRegionPath,
                                                                                                       Class<T> clazz) {
        return Arrays.stream(clazz.getEnumConstants()).map(
                type -> new AbstractMap.SimpleEntry<>(
                        type,
                        GlobalData.getAtlas().findRegions(atlasRegionPath + '/' + type.name())
                ))
                .filter(e -> e.getValue() != null && e.getValue().notEmpty())
                .sorted(Comparator.comparing(e -> e.getValue().first().index))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().toArray()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public static Array<TextureAtlas.AtlasRegion> findAllAnimations(String atlasRegionPath, Class<?> clazz) {
        return new Array<>(Arrays.stream(clazz.getEnumConstants())
                .map(constant -> (Enum<?>) constant)
                .map(type -> GlobalData.getAtlas().findRegions(atlasRegionPath + '/' + type.name()))
                .filter(Objects::nonNull)
                .filter(Array::notEmpty)
                .sorted(Comparator.comparing(region -> region.first().index))
                .map(Array::toArray)
                .flatMap(Stream::of)
                .toArray(TextureAtlas.AtlasRegion[]::new));
    }

    public static AnimationPlaybackImpl<?> loadFromAtlasPath(String atlasPath, Class<?> clazz) {
        var regions = findAllAnimations(atlasPath, clazz);
        var animations = findEnumTypeToIntArrayMapping(atlasPath, clazz);
        return new AnimationPlaybackImpl(regions, animations, clazz);
    }


}
