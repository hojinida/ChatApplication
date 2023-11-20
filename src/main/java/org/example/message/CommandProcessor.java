package org.example.message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.example.ChatServer;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.handler.MessageHandlerMap;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandProcessor {
    private final ReentrantLock lock = new ReentrantLock();
    private final ChatServer chatServer;
    private final Map<Long, ChatRoom> chatRooms;
    private final MessageHandlerMap messageHandlers;
    private final MessageSender messageSender;
    private final Logger logger = Logger.getLogger(CommandProcessor.class.getName());
    private final AtomicLong roomIdSequence;

    public CommandProcessor(ChatServer chatServer) {
        this.chatServer=chatServer;
        this.chatRooms = new ConcurrentHashMap<>();
        this.messageHandlers = new MessageHandlerMap();
        this.messageSender = new MessageSender();
        this.roomIdSequence = new AtomicLong(1);
    }

    public void processMessage(Client client, String message) {
        lock.lock();
        try {
            if (isJson(message)) {
                JsonObject jsonObject = parseJson(message);
                if (jsonObject != null) {
                    processJsonCommand(new Message(client,jsonObject));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private JsonObject parseJson(String message) {
        try {
            return JsonParser.parseString(message).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            logger.log(Level.WARNING, "Invalid JSON format", e);
            return null;
        }
    }

    private boolean isJson(String message) {
        return message.trim().startsWith("{") && message.trim().endsWith("}");
    }

    public void processJsonCommand(Message message) {
        JsonObject jsonObject = message.getJsonObject();
        String type = jsonObject.has("type") ? jsonObject.get("type").getAsString() : "";
        System.out.printf("type : %s\n",type);
        switch (type) {
            case "CSName":
                messageHandlers.get("CSName").handle(this,messageSender,message);
                break;
            case "CSRooms":
                messageHandlers.get("CSRooms").handle(this,messageSender,message);
                break;
            case "CSCreateRoom":
                messageHandlers.get("CSCreateRoom").handle(this,messageSender,message);
                break;
            case "CSJoinRoom":
                messageHandlers.get("CSJoinRoom").handle(this,messageSender,message);
                break;
            case "CSLeaveRoom":
                messageHandlers.get("CSLeaveRoom").handle(this,messageSender,message);
                break;
            case "CSChat":
                logger.log(Level.INFO, "CSChat");
                messageHandlers.get("CSChat").handle(this,messageSender,message);
                break;
            case "CSShutdown":
                messageHandlers.get("CSShutdown").handle(this,messageSender,message);
                break;
        }
    }

    public ChatRoom createRoom(String roomName){
        ChatRoom chatRoom = new ChatRoom(getRoomIdSequence(),roomName);
        chatRooms.put(chatRoom.getId(),chatRoom);
        return chatRoom;
    }

    public ChatRoom getChatRoom(SocketChannel clientChannel,Long roomId) {
        ChatRoom chatRoom = chatRooms.get(roomId);
        if (chatRoom == null) {
            messageSender.sendSystemMessage(clientChannel, "대화방이 존재하지 않습니다.");
            return null;
        }
        return chatRoom;
    }

    public Collection<ChatRoom> getChatRooms() {
        return chatRooms.values();
    }

    private long getRoomIdSequence() {
        return roomIdSequence.getAndIncrement();
    }
    public Client getClient(String clientId) {
        return chatServer.getClient(clientId);
    }
    public void shutdownServer() {
        chatServer.stop();
    }
}