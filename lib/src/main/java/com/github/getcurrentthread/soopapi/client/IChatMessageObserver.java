package com.github.getcurrentthread.soopapi.client;

import com.github.getcurrentthread.soopapi.model.Message;

public interface IChatMessageObserver {
    void notify(Message message);
}