package dev.kabin.util.helperinterfaces;

import org.json.JSONObject;

/**
 * Slap this interface on classes whose state is serializable as json-object.
 */
public interface JSONSerializable {
    JSONObject toJSONObject();
}
