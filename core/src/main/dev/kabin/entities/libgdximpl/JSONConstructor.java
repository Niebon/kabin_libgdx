package dev.kabin.entities.libgdximpl;

import org.json.JSONObject;

@FunctionalInterface
public interface JSONConstructor {
    EntityLibgdx construct(JSONObject jsonObject);
}
