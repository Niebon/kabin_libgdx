package dev.kabin.entities;

import dev.kabin.entities.animation.enums.Tile;
import dev.kabin.entities.impl.CollisionTile;
import dev.kabin.entities.impl.EntityParameters;
import dev.kabin.util.Procedures;
import dev.kabin.util.Statistics;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CollisionTileTest {

    private EntityParameters generateParameters(int x, int y, float scale) {
        return EntityParameters.Builder.testParameters()
                .setX(x * scale)
                .setY(y * scale)
                .setLayer(0)
                .setScale(1)
                .setAtlasPath("raw_textures/ground")
                .put(CollisionTile.FRAME_INDEX, Statistics.RANDOM.nextInt())
                .put(CollisionTile.TILE, Tile.SURFACE.name())
                .setScale(scale)
                .build();
    }

    @Test
    void recordLoad() {
        float scale = 4.8f;
        Procedures.forEachIntInRange(0, 1_000_000, i -> {

            int x = (int) Math.round((Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE);
            int y = (int) Math.round((Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE);

            CollisionTile collisionTileBefore;
            {
                EntityParameters parameters = generateParameters(x, y, scale);
                collisionTileBefore = new CollisionTile(parameters);
            }


            // Before
            int unscaledXPrev = collisionTileBefore.getUnscaledX();
            int unscaledYPrev = collisionTileBefore.getUnscaledY();

            // Record and load:
            CollisionTile.clearAt(collisionTileBefore.getUnscaledX(), collisionTileBefore.getUnscaledY());
            CollisionTile collisionTileAfter;
            {
                JSONObject o = collisionTileBefore.toJSONObject();
                EntityParameters parametersAfterRecordAndLoad = generateParameters(
                        o.getInt("x"),
                        o.getInt("y"),
                        scale
                );
                collisionTileAfter = new CollisionTile(parametersAfterRecordAndLoad);
            }

            // After
            int unscaledX = collisionTileAfter.getUnscaledX();
            int unscaledY = collisionTileAfter.getUnscaledY();

            Assertions.assertEquals(unscaledXPrev, unscaledX);
            Assertions.assertEquals(unscaledYPrev, unscaledY);

            // Clean up before the next iteration.
            CollisionTile.clearAt(collisionTileAfter.getUnscaledX(), collisionTileAfter.getUnscaledY());
        });
    }

}