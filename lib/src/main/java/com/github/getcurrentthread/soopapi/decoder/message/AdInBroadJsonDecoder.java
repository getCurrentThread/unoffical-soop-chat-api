package com.github.getcurrentthread.soopapi.decoder.message;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class AdInBroadJsonDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result = new Gson().fromJson(parts[0], Map.class);

        return result;
    }
}