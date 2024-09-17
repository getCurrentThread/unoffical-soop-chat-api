package com.github.getcurrentthread.afreecatvapi.decoder;

import java.util.Map;

@FunctionalInterface
public interface IMessageDecoder {
    Map<String, Object> decode(String[] parts);
}