package dev.kabin.global;

import dev.kabin.WorldStateRecorder;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

class WorldStateRecorderTest {

    // Can this be redeemed?
    @Disabled
    @Test
    void loadThenRecord() {
        JSONObject content = new JSONObject()
                .put("entities",
                        List.of(new JSONObject()
                                .put("x", 0.0f)
                                .put("y", 1.0f)
                                .put("atlasPath", "raw_textures.player")
                                .put("layer", 10)
                                .put("type", "PLAYER")
                        )
                );
        WorldStateRecorder.loadWorldState(content);
    }

}