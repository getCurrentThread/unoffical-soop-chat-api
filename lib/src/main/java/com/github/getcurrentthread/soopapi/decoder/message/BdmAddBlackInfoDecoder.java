package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.BdmAddBlackInfoEvent;

public class BdmAddBlackInfoDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new BdmAddBlackInfoEvent(
                ChatEvent.BDM_ADD_BLACK_INFO, raw, System.currentTimeMillis());
    }
}
