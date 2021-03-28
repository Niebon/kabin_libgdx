package dev.kabin.entities.animation;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import dev.kabin.entities.animation.enums.Animate;
import dev.kabin.entities.animation.enums.Inanimate;
import dev.kabin.entities.animation.enums.Tile;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnimationBundleFactory {

    private static <T extends Enum<T>> Map<T, int[]> findEnumTypeToIntArrayMapping(TextureAtlas textureAtlas,
                                                                                   String atlasPath,
                                                                                   Class<T> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
                .filter(type -> textureAtlas.findRegions(atlasPath + '/' + type.name()).size > 0)
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> Arrays.stream(textureAtlas.findRegions(atlasPath + '/' + type.name()).toArray())
                                .mapToInt(reg -> reg.index)
                                .toArray()
                ));
    }

    public static <T extends Enum<T>> Map<T, TextureAtlas.AtlasRegion[]> findTypeToAtlasRegionsMapping(
            TextureAtlas textureAtlas,
            String atlasRegionPath,
            Class<T> clazz) {

        return Arrays.stream(clazz.getEnumConstants()).map(
                type -> new AbstractMap.SimpleEntry<>(
                        type,
                        textureAtlas.findRegions(atlasRegionPath + '/' + type.name())
                ))
                .filter(e -> e.getValue() != null && e.getValue().notEmpty())
                .sorted(Comparator.comparing(e -> e.getValue().first().index))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().toArray()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private static Array<TextureAtlas.AtlasRegion> findAllAnimations(
            TextureAtlas textureAtlas,
            String atlasRegionPath,
            Class<?> clazz) {
        return new Array<>(Arrays.stream(clazz.getEnumConstants())
                .map(constant -> (Enum<?>) constant)
                .map(type -> textureAtlas.findRegions(atlasRegionPath + '/' + type.name()))
                .filter(Objects::nonNull)
                .filter(Array::notEmpty)
                .sorted(Comparator.comparing(region -> region.first().index))
                .map(Array::toArray)
                .flatMap(Stream::of)
                .toArray(TextureAtlas.AtlasRegion[]::new));
    }

    @Nullable
    public static <T extends Enum<T>> AbstractAnimationPlayback<T> loadFromAtlasPath(@Nullable TextureAtlas textureAtlas,
                                                                                     String atlasPath,
                                                                                     Class<T> clazz) {
        if (textureAtlas == null) return null;
        if (clazz == Animate.class) {
            var regions = findAllAnimations(textureAtlas, atlasPath, Animate.class);
            var animations = findEnumTypeToIntArrayMapping(textureAtlas, atlasPath, Animate.class);
            return (AbstractAnimationPlayback<T>) new AnimationPlaybackAnimate(textureAtlas, regions, animations);
        } else if (clazz == Inanimate.class) {
            var regions = findAllAnimations(textureAtlas, atlasPath, Inanimate.class);
            var animations = findEnumTypeToIntArrayMapping(textureAtlas, atlasPath, Inanimate.class);
            return (AbstractAnimationPlayback<T>) new AnimationPlaybackInanimate(textureAtlas, regions, animations);
        } else if (clazz == Tile.class) {
            var regions = findAllAnimations(textureAtlas, atlasPath, Tile.class);
            var animations = findEnumTypeToIntArrayMapping(textureAtlas, atlasPath, Tile.class);
            return (AbstractAnimationPlayback<T>) new AnimationPlaybackTile(textureAtlas, regions, animations);
        } else throw new IllegalArgumentException();
    }

    public static class AnimationPlaybackAnimate extends AbstractAnimationPlayback<Animate> {

        public AnimationPlaybackAnimate(TextureAtlas atlas,
                                        Array<TextureAtlas.AtlasRegion> regions,
                                        Map<Animate, int[]> animationBlueprint) {
            super(atlas, regions, animationBlueprint, Animate.class);
        }

        @Override
        public Animate toDefaultAnimation(Animate current) {
            return current.toDefault();
        }

        @Override
        public AnimationMetadata metadataOf(Animate animationEnum) {
            return animationEnum.getMetadata();
        }

    }

    public static class AnimationPlaybackInanimate extends AbstractAnimationPlayback<Inanimate> {

        public AnimationPlaybackInanimate(TextureAtlas atlas,
                                          Array<TextureAtlas.AtlasRegion> regions,
                                          Map<Inanimate, int[]> animationBlueprint) {
            super(atlas, regions, animationBlueprint, Inanimate.class);
        }

        @Override
        public Inanimate toDefaultAnimation(Inanimate current) {
            return Inanimate.DEFAULT;
        }

        @Override
        public AnimationMetadata metadataOf(Inanimate animationEnum) {
            return Inanimate.ANIMATION_METADATA;
        }

    }

    public static class AnimationPlaybackTile extends AbstractAnimationPlayback<Tile> {

        public AnimationPlaybackTile(TextureAtlas atlas,
                                     Array<TextureAtlas.AtlasRegion> regions,
                                     Map<Tile, int[]> animationBlueprint) {
            super(atlas, regions, animationBlueprint, Tile.class);
        }

        @Override
        public Tile toDefaultAnimation(Tile current) {
            return current;
        }

        @Override
        public AnimationMetadata metadataOf(Tile animationEnum) {
            return Tile.ANIMATION_METADATA;
        }

    }

}
