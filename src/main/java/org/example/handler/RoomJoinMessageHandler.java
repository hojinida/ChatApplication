package org.example.handler;

import com.google.gson.JsonObject;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.client.ClientState;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;

public class RoomJoinMessageHandler implements MessageHandler{
    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        ClientState clientState = client.getClientState();
        SocketChannel clientChannel = client.getClientChannel();
        JsonObject jsonObject = message.getJsonObject();

        Long roomId = jsonObject.get("roomId").getAsLong();

        if(clientState.getRoomId()!=null){
            messageSender.sendSystemMessage(clientChannel, "대화 방에 있을 때는 다른 방에 입장할 수 없습니다.");
        }

        ChatRoom chatRoom = commandProcessor.getChatRoom(clientChannel,roomId);
        if(chatRoom == null){
            return;
        }
        chatRoom.addClient(client);
        clientState.setRoomId(roomId);
        messageSender.sendSystemMessage(clientChannel, "방제[" + chatRoom.getName() + "] 방에 입장했습니다.");

        for(String clientId : chatRoom.getClientIds()){
            Client roomClient = commandProcessor.getClient(clientId);
            if (!roomClient.equals(client)){
                messageSender.sendSystemMessage(roomClient.getClientChannel(), "[" + clientState.getNickname() + "]님이 입장했습니다.");
            }
        }
    }
}
