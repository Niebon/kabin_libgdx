package dev.kabin.entities;

import dev.kabin.animation.AnimationBundleFactory;
import dev.kabin.animation.AnimationClass;

import java.util.HashMap;
import java.util.Map;

public class CollisionTile extends CollisionEntity {

    private static final Map<String, Map<AnimationClass.Tile, int[]>> STRING_MAP_HASH_MAP = new HashMap<>();
    public static final String FRAME_INDEX = "frameIndex";
    public static final String TYPE = "type";


    CollisionTile(EntityParameters parameters) {
        super(parameters);
        final String atlasPath = parameters.atlasPath();
        STRING_MAP_HASH_MAP.putIfAbsent(atlasPath, AnimationBundleFactory.findEnumTypeToIntArrayMapping(atlasPath, AnimationClass.Tile.class));
        final AnimationClass.Tile tile = parameters.get(TYPE, AnimationClass.Tile.class).orElseThrow();
        final int index = parameters.get(FRAME_INDEX, Integer.class).orElseThrow();

    }


}
