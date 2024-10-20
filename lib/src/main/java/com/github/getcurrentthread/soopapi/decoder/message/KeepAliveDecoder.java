package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class KeepAliveDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        return new HashMap<>();
    }
}