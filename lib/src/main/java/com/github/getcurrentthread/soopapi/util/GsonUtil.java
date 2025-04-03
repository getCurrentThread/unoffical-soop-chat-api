package com.github.getcurrentthread.soopapi.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class GsonUtil {
    private static final Gson gson = createCustomGson();

    private static Gson createCustomGson() {
        return new GsonBuilder()
                .registerTypeAdapter(
                        new TypeToken<Map<String, Object>>() {}.getType(), new MapDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public static Map<String, Object> fromJson(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
    }

    private static class MapDeserializer implements JsonDeserializer<Map<String, Object>> {
        @SuppressWarnings("unchecked")
        @Override
        public Map<String, Object> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return (Map<String, Object>) ParseObjectFromElement.INSTANCE.apply(json);
        }
    }

    private enum ParseObjectFromElement implements Function<JsonElement, Object> {
        INSTANCE;

        @Override
        public Object apply(JsonElement input) {
            if (input == null || input.isJsonNull()) {
                return null;
            } else if (input.isJsonPrimitive()) {
                JsonPrimitive primitive = input.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    Number number = primitive.getAsNumber();
                    // Integer로 정확히 표현 가능한 경우 Integer로 반환
                    if (number.doubleValue() == number.intValue()) {
                        return number.intValue();
                    }
                    // Long으로 정확히 표현 가능한 경우 Long으로 반환
                    if (number.doubleValue() == number.longValue()) {
                        return number.longValue();
                    }
                    // 그 외의 경우 Double로 반환
                    return number.doubleValue();
                } else if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                } else {
                    return primitive.getAsString();
                }
            } else if (input.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonElement element : input.getAsJsonArray()) {
                    list.add(apply(element));
                }
                return list;
            } else if (input.isJsonObject()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : input.getAsJsonObject().entrySet()) {
                    map.put(entry.getKey(), apply(entry.getValue()));
                }
                return map;
            }
            throw new JsonParseException(
                    "Unexpected JSON type: " + input.getClass().getSimpleName());
        }
    }
}
