package org.example.handler;

import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.client.ClientState;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;

public class RoomLeaveMessageHandler implements MessageHandler{
    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        ClientState clientState = client.getClientState();
        SocketChannel clientChannel = client.getClientChannel();

        if(clientState.getRoomId() == null){
            messageSender.sendSystemMessage(clientChannel, "현재 대화방에 들어가 있지 않습니다.");
        }
        ChatRoom chatRoom = commandProcessor.getChatRoom(clientChannel,clientState.getRoomId());
        if(chatRoom == null){
            return;
        }
        chatRoom.removeClient(client);
        clientState.setRoomId(null);
        messageSender.sendSystemMessage(clientChannel, "방제["+chatRoom.getName()+"] 대화 방에서 퇴장했습니다.");

        for(String clientId : chatRoom.getClientIds()){
            Client roomClient = commandProcessor.getClient(clientId);
            if (!roomClient.equals(client)){
                messageSender.sendSystemMessage(roomClient.getClientChannel(), "[" + clientState.getNickname() + "] 대화 방에서 퇴장했습니다.");
            }
        }
    }
}
