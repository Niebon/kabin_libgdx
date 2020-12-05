package dev.kabin.entities;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityCollectionProvider {

    private static final Map<Type, List<Entity>> groupMap = new EnumMap<>(Type.class);

    private static final List<Entity> backgroundEntityImageViews = new ArrayList<>();

    private static final List<Entity> backgroundEntityLayer2ImageViews = new ArrayList<>();

    private static final List<Entity> groundTileImageViews = new ArrayList<>();

    private static final List<Entity> focalPointEntityImageViews = new ArrayList<>();

    private static final List<Entity> foregroundEntityImageViews = new ArrayList<>();

    private static final List<Entity> cloudLayer1 = new ArrayList<>();

    private static final List<Entity> cloudLayer2 = new ArrayList<>();

    private static final List<Entity> staticBackground = new ArrayList<>();

    private static final List<Entity> sky = new ArrayList<>();

    private static final Type[] GROUPS_ORDERED = Arrays.stream(Type.values())
            .sorted(Comparator.comparingInt(Type::getLayer))
            .toArray(Type[]::new);

    static {
        groupMap.put(Type.BACKGROUND, backgroundEntityImageViews);
        groupMap.put(Type.BACKGROUND_LAYER_2, backgroundEntityLayer2ImageViews);
        groupMap.put(Type.GROUND, groundTileImageViews);
        groupMap.put(Type.FOCAL_POINT, focalPointEntityImageViews);
        groupMap.put(Type.FOREGROUND, foregroundEntityImageViews);
        groupMap.put(Type.CLOUDS, cloudLayer1);
        groupMap.put(Type.CLOUDS_LAYER_2, cloudLayer2);
        groupMap.put(Type.STATIC_BACKGROUND, staticBackground);
        groupMap.put(Type.SKY, sky);
    }

    public static void registerEntity(Entity e) {
        groupMap.get(e.getType().groupType).add(e);
    }

    public static boolean unregisterEntity(Entity e) {
        return groupMap.get(e.getType().groupType).remove(e);
    }

    public static void actionForEachEntityInGroup(Type groupType, Consumer<Entity> action) {
        List<Entity> entities = groupMap.get(groupType);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = entities.size(); i < n; i++) {
            action.accept(entities.get(i));
        }
    }

    public static void actionForEachEntityOrderedByGroup(Consumer<Entity> action) {
        for (Type type : GROUPS_ORDERED) {
            actionForEachEntityInGroup(type, action);
        }
    }

    /**
     * Populate the given collection by dev.kabin.entities matching the criterion such that no objects are created.
     *
     * @param collection the collection to be populated.
     * @param criterion  the criterion to match.
     */
    public static void populateCollection(Collection<Entity> collection, Predicate<Entity> criterion) {
        for (Type type : GROUPS_ORDERED) {
            final List<Entity> list = groupMap.get(type);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = list.size(); i < n; i++) {
                if (criterion.test(list.get(i))) {
                    collection.add(list.get(i));
                }
            }
        }
    }

    public enum Type {
        SKY(-5),
        CLOUDS_LAYER_2(-4),
        CLOUDS(-4),
        STATIC_BACKGROUND(-3),
        BACKGROUND_LAYER_2(-2),
        BACKGROUND(-1),
        FOCAL_POINT(0),
        GROUND(1),
        FOREGROUND(2);

        private final int layer;

        Type(int layer) {
            this.layer = layer;
        }

        public int getLayer() {
            return layer;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
