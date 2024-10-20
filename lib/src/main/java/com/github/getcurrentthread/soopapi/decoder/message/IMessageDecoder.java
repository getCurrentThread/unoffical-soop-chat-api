package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.Map;

@FunctionalInterface
public interface IMessageDecoder {
    Map<String, Object> decode(String[] parts);
}