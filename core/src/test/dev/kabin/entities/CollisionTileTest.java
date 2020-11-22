package dev.kabin.entities;

import dev.kabin.animation.AnimationClass;
import dev.kabin.utilities.Procedures;
import dev.kabin.utilities.Statistics;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CollisionTileTest {

    private EntityParameters generateParameters(int x, int y, float scale) {
        return new EntityParameters.Builder()
                .setX(x * scale)
                .setY(y * scale)
                .setLayer(0)
                .setScale(1)
                .setAtlasPath("raw_textures/ground")
                .put(CollisionTile.FRAME_INDEX, Statistics.RANDOM.nextInt())
                .put(CollisionTile.TILE, AnimationClass.Tile.SURFACE.name())
                .setContext(EntityParameters.Context.TEST)
                .build();
    }

    @Test
    void recordLoad() {
        float scale = 4.8f;
        Procedures.forEachIntInRange(0, 100_000, i -> {

            CollisionTile collisionTileBefore;
            {
                int x = (int) Math.round(Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE;
                int y = (int) Math.round(Math.random() - 0.5) * 3 * CollisionTile.TILE_SIZE;
                var parameters = generateParameters(x, y, scale);
                collisionTileBefore = new CollisionTile(parameters);
            }


            // Before
            int unscaledXPrev = collisionTileBefore.getUnscaledX();
            int unscaledYPrev = collisionTileBefore.getUnscaledY();

            // Record and load:
            CollisionTile collisionTileAfter;
            {
                JSONObject o = collisionTileBefore.toJSONObject();
                var parametersAfterRecordAndLoad = generateParameters(o.getInt("x"), o.getInt("y"), scale);
                collisionTileAfter = new CollisionTile(parametersAfterRecordAndLoad);
            }

            // After
            int unscaledX = collisionTileAfter.getUnscaledX();
            int unscaledY = collisionTileAfter.getUnscaledY();

            Assertions.assertEquals(unscaledXPrev, unscaledX);
            Assertions.assertEquals(unscaledYPrev, unscaledY);
        });
    }

}