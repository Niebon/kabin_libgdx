package dev.kabin.entities.impl;

import org.json.JSONObject;

@FunctionalInterface
public interface JsonConstructor {
    EntityLibgdx construct(JSONObject jsonObject);
}
