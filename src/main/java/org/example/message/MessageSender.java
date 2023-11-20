package org.example.message;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSender {
    private final Logger logger;

    public MessageSender() {
        this.logger = Logger.getLogger(MessageSender.class.getName());
    }

    public void sendChatMessage(SocketChannel clientChannel, String memberName, String message) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "SCChat");
        jsonMessage.addProperty("member", memberName);
        jsonMessage.addProperty("text", message);
        sendJsonMessage(clientChannel, jsonMessage);
    }

    public void sendSystemMessage(SocketChannel clientChannel, String message) {
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type", "SCSystemMessage");
        jsonMessage.addProperty("text", message);
        sendJsonMessage(clientChannel, jsonMessage);
    }

    public void sendJsonMessage(SocketChannel clientChannel, JsonObject jsonMessage) {
        try {
            String jsonString = jsonMessage.toString();
            byte[] messageBytes = jsonString.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(2 + messageBytes.length);
            buffer.putShort((short) messageBytes.length);
            buffer.put(messageBytes);
            buffer.flip();
            while (buffer.hasRemaining()) {
                clientChannel.write(buffer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "메시지 전송 오류", e);
        }
    }
}
