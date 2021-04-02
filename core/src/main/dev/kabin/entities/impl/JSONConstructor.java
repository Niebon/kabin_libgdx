package dev.kabin.entities.impl;

import org.json.JSONObject;

@FunctionalInterface
public interface JSONConstructor {
    EntityLibgdx construct(JSONObject jsonObject);
}
