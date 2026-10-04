package io.github.rollylindenshnizzer.nexuscore.api.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NexusConfigMetadataValue<T> {
    private final String path;
    private final T value;

    NexusConfigMetadataValue(String path, T value) {
        this.path = path;
        this.value = value;
    }

    public String path() {
        return path;
    }

    public T get() {
        return value;
    }

    JsonElement toJson() {
        if (value instanceof Boolean booleanValue) {
            return new JsonPrimitive(booleanValue);
        }
        if (value instanceof Number numberValue) {
            return new JsonPrimitive(numberValue);
        }
        return new JsonPrimitive(String.valueOf(value));
    }
}
