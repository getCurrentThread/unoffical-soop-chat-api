package com.github.getcurrentthread.soopapi.decoder.factory;

import com.github.getcurrentthread.soopapi.decoder.message.IMessageDecoder;
import com.github.getcurrentthread.soopapi.model.MessageType;

import java.util.Map;

public interface MessageDecoderFactory {
    Map<MessageType, IMessageDecoder> createDecoders();
}