package com.github.getcurrentthread.soopapi.decoder.factory;

import java.util.Map;

import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.model.MessageType;

public interface MessageDecoderFactory {
    Map<MessageType, IMessageDecoder> createDecoders();
}
