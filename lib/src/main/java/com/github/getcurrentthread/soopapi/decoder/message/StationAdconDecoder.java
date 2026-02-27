package com.github.getcurrentthread.soopapi.decoder.message;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.StationAdconEvent;

public class StationAdconDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        if (parts.length >= 7) {
            return new StationAdconEvent(
                    parts[0],
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    parts[4],
                    parts[5],
                    parts[6],
                    ChatEvent.STATION_ADCON,
                    raw,
                    System.currentTimeMillis());
        }
        return new StationAdconEvent(
                "",
                "",
                "",
                0,
                "",
                "",
                "",
                ChatEvent.STATION_ADCON,
                raw,
                System.currentTimeMillis());
    }
}
