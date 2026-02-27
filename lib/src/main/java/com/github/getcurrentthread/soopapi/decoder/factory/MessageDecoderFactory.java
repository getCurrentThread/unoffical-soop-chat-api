package com.github.getcurrentthread.soopapi.decoder.factory;

import java.util.Map;

import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.event.ChatEvent;

public interface MessageDecoderFactory {
    Map<ChatEvent, IMessageDecoder> createDecoders();
}
