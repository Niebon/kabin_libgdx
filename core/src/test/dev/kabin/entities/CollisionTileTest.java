package dev.kabin.entities;

import dev.kabin.entities.libgdximpl.CollisionTile;
import dev.kabin.entities.libgdximpl.EntityParameters;
import dev.kabin.entities.libgdximpl.EntityType;
import dev.kabin.entities.libgdximpl.animation.enums.Tile;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

class CollisionTileTest {

    private EntityParameters generateParameters(int x, int y) {
        return EntityParameters.builder()
                .setX(x)
                .setY(y)
                .setLayer(0)
                .setAtlasPath("raw_textures/ground")
                .put(CollisionTile.FRAME_INDEX, new Random().nextInt())
                .put(CollisionTile.TILE, Tile.SURFACE.name())
                .setEntityType(EntityType.COLLISION_TILE)
                .build();
    }

    @Test
    void recordLoad() {
        IntStream.range(0, 1_000_000).forEach(i -> {

            int x = (int) Math.round((Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE);
            int y = (int) Math.round((Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE);

            CollisionTile collisionTileBefore;
            {
                EntityParameters parameters = generateParameters(x, y);
                collisionTileBefore = new CollisionTile(parameters);
            }


            // Before
            int unscaledXPrev = collisionTileBefore.getXAsInt();
            int unscaledYPrev = collisionTileBefore.getYAsInt();

            // Record and load:
            CollisionTile.clearAt(collisionTileBefore.getXAsInt(), collisionTileBefore.getYAsInt());
            CollisionTile collisionTileAfter;
            {
                JSONObject o = collisionTileBefore.toJSONObject();
                EntityParameters parametersAfterRecordAndLoad = generateParameters(
                        o.getInt("x"),
                        o.getInt("y")
                );
                collisionTileAfter = new CollisionTile(parametersAfterRecordAndLoad);
            }

            // After
            int unscaledX = collisionTileAfter.getXAsInt();
            int unscaledY = collisionTileAfter.getYAsInt();

            Assertions.assertEquals(unscaledXPrev, unscaledX);
            Assertions.assertEquals(unscaledYPrev, unscaledY);

            // Clean up before the next iteration.
            CollisionTile.clearAt(collisionTileAfter.getXAsInt(), collisionTileAfter.getYAsInt());
        });
    }

}