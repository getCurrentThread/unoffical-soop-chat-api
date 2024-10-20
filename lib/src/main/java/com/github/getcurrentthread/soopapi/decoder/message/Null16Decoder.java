package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class Null16Decoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        return new HashMap<>(); // This message type doesn't seem to have any data
    }
}