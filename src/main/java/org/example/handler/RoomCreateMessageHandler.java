package org.example.handler;

import com.google.gson.JsonObject;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.client.ClientState;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;

public class RoomCreateMessageHandler implements MessageHandler {
    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        ClientState clientState = client.getClientState();
        SocketChannel clientChannel = client.getClientChannel();
        JsonObject jsonObject = message.getJsonObject();

        String roomName = jsonObject.get("title").getAsString();

        if(clientState.getRoomId() != null){
            messageSender.sendSystemMessage(clientChannel, "대화 방에 있을 때는 방을 개설 할 수 없습니다.");
        }
        ChatRoom chatRoom = commandProcessor.createRoom(roomName);
        chatRoom.addClient(client);
        clientState.setRoomId(chatRoom.getId());
        messageSender.sendSystemMessage(clientChannel, "방제[" + roomName + "] 방에 입장했습니다.");
    }
}
