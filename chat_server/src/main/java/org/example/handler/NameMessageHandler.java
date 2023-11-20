package org.example.handler;

import com.google.gson.JsonObject;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.client.ClientState;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NameMessageHandler implements MessageHandler {
    private final Logger logger;

    public NameMessageHandler() {
        this.logger = Logger.getLogger(NameMessageHandler.class.getName());
    }

    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        ClientState clientState = client.getClientState();
        SocketChannel clientChannel = client.getClientChannel();
        JsonObject jsonObject = message.getJsonObject();

        String name = jsonObject.get("name").getAsString();
        String oldName = clientState.getNickname();
        clientState.setNickname(name);

        // 닉네임이 변경된 클라이언트에게 메시지 전송
        messageSender.sendSystemMessage(clientChannel, "이름이 " + name + "으로 변경되었습니다.");

        // 현재 채팅방에 있는 경우, 채팅방의 모든 멤버에게 알림 전송
        if (clientState.getRoomId() != null) {
            ChatRoom chatRoom = commandProcessor.getChatRoom(clientChannel,clientState.getRoomId());
            if(chatRoom !=null) {
                for (String clientId : chatRoom.getClientIds()) {
                    logger.log(Level.INFO, clientId);
                    Client roomClient = commandProcessor.getClient(clientId);
                    if (roomClient != null) {
                        messageSender.sendSystemMessage(roomClient.getClientChannel(), "[" + oldName + "]의 이름이 " + name + "으로 변경되었습니다.");
                    }
                }
            }
        }
    }
}
