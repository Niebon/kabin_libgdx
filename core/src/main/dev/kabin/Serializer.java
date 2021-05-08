package dev.kabin;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import dev.kabin.components.WorldRepresentation;
import dev.kabin.entities.Entity;
import dev.kabin.entities.libgdximpl.EntityGroup;
import dev.kabin.entities.libgdximpl.EntityLibgdx;
import dev.kabin.entities.libgdximpl.EntityType;
import dev.kabin.entities.libgdximpl.animation.imageanalysis.ImageMetadataPoolLibgdx;
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

    public static JSONObject recordWorldState(WorldRepresentation<EntityGroup, EntityLibgdx> worldRepresentation) {
        final List<EntityLibgdx> allEntities = new ArrayList<>();
        worldRepresentation.populateCollection(allEntities, e -> true);
        JSONObject o = new JSONObject();
        o.put(ENTITIES, allEntities.stream().map(Entity::toJSONObject).toList());
        o.put(WORLD_SIZE_X, worldRepresentation.getWorldSizeX());
        o.put(WORLD_SIZE_Y, worldRepresentation.getWorldSizeY());
        return o;
    }

    public static WorldRepresentation<EntityGroup, EntityLibgdx> loadWorldState(Stage stage,
                                                                                TextureAtlas textureAtlas,
                                                                                ImageMetadataPoolLibgdx imageAnalysisPool,
                                                                                JSONObject o) {
        final HashSet<String> admissibleEntityTypes = Arrays.stream(EntityType.values()).map(Enum::name)
                .collect(Collectors.toCollection(HashSet::new));
        final var worldRepresentation = new WorldRepresentation<EntityGroup, EntityLibgdx>(EntityGroup.class, o.getInt(WORLD_SIZE_X), o.getInt(WORLD_SIZE_Y));
        o.getJSONArray(ENTITIES).iterator().forEachRemaining(entry -> {
            if (!(entry instanceof final JSONObject json)) {
                logger.warning(() -> "A recorded entity was not saved as a JSON object: " + entry);
                System.exit(1);
            } else {
                final String type = json.getString("type");
                if (!admissibleEntityTypes.contains(type)) {
                    logger.warning(() -> "A recorded entity type was inadmissible." + json);
                    System.exit(1);
                } else {
                    logger.info(() -> "Loaded the entity: " + json);
                    final EntityLibgdx e = EntityType.Factory.JSONConstructorOf(EntityType.valueOf(type),
                            textureAtlas,
                            imageAnalysisPool
                    ).construct(json);
                    worldRepresentation.registerEntity(e);
                    e.getActor().ifPresent(stage::addActor);
                }
            }
        });

        return worldRepresentation;
    }


}
