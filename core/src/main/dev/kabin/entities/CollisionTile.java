package dev.kabin.entities;

import dev.kabin.animation.AnimationBundleFactory;

import java.util.HashMap;
import java.util.Map;

public class CollisionTile extends CollisionEntity {

    private static final Map<String, Map<Type, int[]>> assetToFramesMap = new HashMap<>();
    public static final String FRAME_INDEX = "frameIndex";


    CollisionTile(EntityParameters parameters) {
        super(parameters);
        final String atlasPath = parameters.atlasPath();
        assetToFramesMap.putIfAbsent(atlasPath, AnimationBundleFactory.findEnumTypeToIntArrayMapping(atlasPath, Type.values()));
        final Type type = parameters.get("type", Type.class).orElseThrow();
        final int index = parameters.get(FRAME_INDEX, Integer.class).orElseThrow();
    }


    public enum Type {
        SURFACE,
        DIAGONAL_45,
        DIAGONAL_135,
        INNER,
        INNER_45,
        INNER_135
    }

}
