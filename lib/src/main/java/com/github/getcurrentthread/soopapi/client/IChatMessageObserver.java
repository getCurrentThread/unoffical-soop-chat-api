package com.github.getcurrentthread.soopapi.client;

import java.util.Map;

public interface IChatMessageObserver {
    void notify(Map<String, Object> message);
}