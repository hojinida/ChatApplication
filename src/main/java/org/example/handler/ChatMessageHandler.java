package org.example.handler;

import com.google.gson.JsonObject;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.client.ClientState;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;

public class ChatMessageHandler implements MessageHandler{
    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        SocketChannel clientChannel = client.getClientChannel();
        ClientState clientState = client.getClientState();
        JsonObject jsonObject = message.getJsonObject();

        String text = jsonObject.get("text").getAsString();
        if(clientState.getRoomId() == null){
            messageSender.sendSystemMessage(client.getClientChannel(), "현재 대화방에 들어가 있지 않습니다.");
            return;
        }

        ChatRoom chatRoom = commandProcessor.getChatRoom(clientChannel,clientState.getRoomId());
        if(chatRoom == null){
            return;
        }

        for(String clientId : chatRoom.getClientIds()){
            Client roomClient = commandProcessor.getClient(clientId);
            if (!roomClient.equals(client)){
                messageSender.sendChatMessage(roomClient.getClientChannel(), clientState.getNickname(), text);
            }
        }
    }
}
