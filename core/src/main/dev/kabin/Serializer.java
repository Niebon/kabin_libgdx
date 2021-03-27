package dev.kabin;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.impl.Entity;
import dev.kabin.entities.impl.EntityFactory;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Serializer {

    private static final Logger logger = Logger.getLogger(Serializer.class.getName());
    public static final String WORLD_SIZE_X = "worldSizeX";
    public static final String WORLD_SIZE_Y = "worldSizeY";
    public static final String ENTITIES = "entities";

    public static JSONObject recordWorldState(WorldRepresentation worldRepresentation, float scale) {
        final List<Entity> allEntities = new ArrayList<>();
        worldRepresentation.populateCollection(allEntities, e -> true);
        JSONObject o = new JSONObject();
        o.put(ENTITIES, allEntities.stream().map(Entity::toJSONObject).collect(Collectors.toList()));
        o.put(WORLD_SIZE_X, GlobalData.worldSizeX);
        o.put(WORLD_SIZE_Y, GlobalData.worldSizeY);
        return o;
    }

    public static WorldRepresentation loadWorldState(Stage stage, TextureAtlas textureAtlas, JSONObject o, float scale) {
        final HashSet<String> admissibleEntityTypes = Arrays.stream(EntityFactory.EntityType.values()).map(Enum::name)
                .collect(Collectors.toCollection(HashSet::new));
        final var worldRepresentation = new WorldRepresentation(o.getInt(WORLD_SIZE_X), o.getInt(WORLD_SIZE_Y), scale);
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
                    Entity e = EntityFactory.EntityType.valueOf(primitiveType).getJsonConstructor(textureAtlas, scale).construct(json);
                    worldRepresentation.registerEntity(e);
                    e.getActor().ifPresent(stage::addActor);
                }
            }
        });

        return worldRepresentation;
    }


}
