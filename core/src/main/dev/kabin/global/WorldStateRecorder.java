package dev.kabin.global;

import dev.kabin.entities.Entity;
import dev.kabin.entities.EntityFactory;
import dev.kabin.entities.EntityGroupProvider;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldStateRecorder {

    private static final Logger logger = Logger.getLogger(WorldStateRecorder.class.getName());

    public static JSONObject recordWorldState() {
        final List<Entity> allEntities = new ArrayList<>();
        EntityGroupProvider.populateCollection(allEntities, e -> true);
        JSONObject o = new JSONObject();
        o.put("entities", allEntities.stream().map(Entity::toJSONObject).collect(Collectors.toList()));
        return o;
    }

    public static void loadWorldState(JSONObject o) {
        final HashSet<String> set = Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name)
                .collect(Collectors.toCollection(HashSet::new));
        o.getJSONArray("entities").iterator().forEachRemaining(entry -> {
            if (!(entry instanceof JSONObject)) {
                logger.warning(() -> "A recorded entity was not saved as a JSON object: " + entry);
            } else {
                JSONObject json = (JSONObject) entry;
                String type = json.getString("type");
                if (!set.contains(type)) {
                    logger.warning(() -> "A recorded entity was not saved as a JSON object." + json);
                } else {
                    logger.info(() -> "Loaded the entity: " + json);
                    Entity e = EntityFactory.EntityType.valueOf(type).getJsonConstructor().construct(json);
                    EntityGroupProvider.registerEntity(e);
                }
            }
        });
    }


}
