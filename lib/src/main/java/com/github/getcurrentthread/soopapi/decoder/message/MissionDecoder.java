package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.util.GsonUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MissionDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = GsonUtil.fromJson(parts[0]);
        return result;
    }
}