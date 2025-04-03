package com.github.getcurrentthread.soopapi.model;

import java.util.Map;

public class Message {
    private final MessageType type;
    private final Map<String, Object> data;
    private final String _raw;

    public Message(MessageType type, Map<String, Object> data, String raw) {
        this.type = type;
        this.data = data;
        this._raw = raw;
    }

    public MessageType getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String _getRaw() {
        return _raw;
    }

    @Override
    public String toString() {
        return "Message{" + "type=" + type + ", data=" + data + ", raw=" + _raw + '}';
    }
}
