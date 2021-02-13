package dev.kabin;

import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityCollectionProvider;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldStateRecorder {

    private static final Logger logger = Logger.getLogger(WorldStateRecorder.class.getName());
    public static final String WORLD_SIZE_X = "worldSizeX";
    public static final String WORLD_SIZE_Y = "worldSizeY";
    public static final String ENTITIES = "entities";

    public static JSONObject recordWorldState() {
        final List<Entity> allEntities = new ArrayList<>();
        GlobalData.getWorldRepresentation().populateCollection(allEntities, e -> true);
        JSONObject o = new JSONObject();
        o.put(ENTITIES, allEntities.stream().map(Entity::toJSONObject).collect(Collectors.toList()));
        o.put(WORLD_SIZE_X, GlobalData.worldSizeX);
        o.put(WORLD_SIZE_Y, GlobalData.worldSizeY);
        return o;
    }

    public static void loadWorldState(JSONObject o) {
        final HashSet<String> admissibleEntityTypes = Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name)
                .collect(Collectors.toCollection(HashSet::new));
        GlobalData.setMapSize(o.getInt(WORLD_SIZE_X), o.getInt(WORLD_SIZE_Y));
        o.getJSONArray(ENTITIES).iterator().forEachRemaining(entry -> {
            if (!(entry instanceof JSONObject)) {
                logger.warning(() -> "A recorded entity was not saved as a JSON object: " + entry);
                System.exit(1);
            } else {
                JSONObject json = (JSONObject) entry;
                String primitiveType = json.getString("primitiveType");
                if (!admissibleEntityTypes.contains(primitiveType)) {
                    logger.warning(() -> "A recorded entity primitiveType was inadmissible." + json);
                    System.exit(1);
                } else {
                    logger.info(() -> "Loaded the entity: " + json);
                    Entity e = EntityFactory.EntityType.valueOf(primitiveType).getJsonConstructor().construct(json);
                    GlobalData.getWorldRepresentation().registerEntity(e);
                    e.getActor().ifPresent(GlobalData.stage::addActor);
                }
            }
        });

        // Now that a world is loaded, begin threads!
        Threads.init();
    }


}
