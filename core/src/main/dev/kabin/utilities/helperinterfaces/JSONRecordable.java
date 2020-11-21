package dev.kabin.utilities.helperinterfaces;

import org.json.JSONObject;

/**
 * Slap this interface on classes whose state is recordable as json-object.
 */
public interface JSONRecordable {
    JSONObject toJSONObject();
}
