package ua.lil.chat.listener;

import ua.lil.chat.protocol.AbstractPacket;

public interface Listener {
    public void handle(AbstractPacket abstractPacket);
}

