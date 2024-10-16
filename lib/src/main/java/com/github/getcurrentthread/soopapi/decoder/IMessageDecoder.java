package com.github.getcurrentthread.soopapi.decoder;

import java.util.Map;

@FunctionalInterface
public interface IMessageDecoder {
    Map<String, Object> decode(String[] parts);
}