package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.ArrayList;
import java.util.List;

import com.github.getcurrentthread.soopapi.event.ChatEvent;
import com.github.getcurrentthread.soopapi.event.model.BaseEvent;
import com.github.getcurrentthread.soopapi.event.model.KickUserListEvent;

public class KickUserListDecoder implements IMessageDecoder {
    @Override
    public BaseEvent decode(String[] parts, String raw) {
        List<KickUserListEvent.KickedUser> kickedUsers = new ArrayList<>();
        for (int i = 0; i + 6 <= parts.length; i += 6) {
            kickedUsers.add(
                    new KickUserListEvent.KickedUser(
                            parts[i],
                            parts[i + 1],
                            parts[i + 2],
                            parts[i + 3],
                            parts[i + 4],
                            parts[i + 5]));
        }
        return new KickUserListEvent(
                kickedUsers, ChatEvent.KICK_USERLIST, raw, System.currentTimeMillis());
    }
}
