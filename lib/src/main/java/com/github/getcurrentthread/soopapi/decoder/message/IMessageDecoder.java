package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.model.BaseEvent;

@FunctionalInterface
public interface IMessageDecoder {
    BaseEvent decode(String[] parts, String raw);
}
