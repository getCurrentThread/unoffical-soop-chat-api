package com.github.getcurrentthread.afreecatvapi.client;

import java.util.Map;

public interface IChatMessageObserver {
    void notify(Map<String, Object> message);
}