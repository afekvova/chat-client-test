package ua.lil.chat.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import ua.lil.chat.protocol.AbstractPacket;

import java.util.Collection;
import java.util.Iterator;

public class ListenerManager {
    
    private final Multimap<Class<? extends AbstractPacket>, Listener> listeners = Multimaps.synchronizedMultimap((Multimap) HashMultimap.create());

    public static void info(Object msg) {
        System.out.println("[CONNECTOR] " + msg);
    }

    public Listener registerListener(Class<? extends AbstractPacket> packetClass, Listener listener) {
        this.listeners.put(packetClass, listener);
        ListenerManager.info("New listener for packet " + packetClass.getSimpleName() + " registed!");
        return listener;
    }

    public void unregisterListener(Class<? extends AbstractPacket> packetClass, Listener listener) {
        if (this.listeners.remove(packetClass, listener)) {
            ListenerManager.info("Listener for packet " + packetClass.getSimpleName() + " unregisted!");
        } else {
            ListenerManager.info("Cannot find listener for " + packetClass.getSimpleName() + " packet!");
        }
    }

    public void unregisterListeners(Class<? extends AbstractPacket> packetClass) {
        this.listeners.removeAll(packetClass);
        ListenerManager.info("All listeners for " + packetClass.getSimpleName() + " packet unregisted!");
    }

    public void handleListeners(AbstractPacket packet) {
        Collection listeners = this.listeners.get(packet.getClass());
        if (listeners != null) {
            Iterator iterator = listeners.iterator();
            while (iterator.hasNext()) {
                ((Listener) iterator.next()).handle(packet);
            }
        }
    }
}

