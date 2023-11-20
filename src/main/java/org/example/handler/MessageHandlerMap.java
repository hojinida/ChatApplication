package org.example.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandlerMap {
    private final Map<String, MessageHandler> handlers;


    public MessageHandlerMap() {
        handlers = new ConcurrentHashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put("CSName", new NameMessageHandler());
        handlers.put("CSRooms",new RoomsMessageHandler());
        handlers.put("CSCreateRoom", new RoomCreateMessageHandler());
        handlers.put("CSJoinRoom", new RoomJoinMessageHandler());
        handlers.put("CSLeaveRoom", new RoomLeaveMessageHandler());
        handlers.put("CSChat", new ChatMessageHandler());
        handlers.put("CSShutdown", new ShutdownHandler());
    }

    public MessageHandler get(String type) {
        return handlers.get(type);
    }
}
