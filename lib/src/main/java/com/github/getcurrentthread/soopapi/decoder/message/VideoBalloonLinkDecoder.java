package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.VideoBalloonLinkEvent;

public class VideoBalloonLinkDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        return new VideoBalloonLinkEvent(
                ChatEvent.VIDEO_BALLOON_LINK, raw, System.currentTimeMillis());
    }
}
