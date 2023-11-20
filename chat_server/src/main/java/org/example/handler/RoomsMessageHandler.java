package org.example.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.client.ChatRoom;
import org.example.client.Client;
import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

import java.nio.channels.SocketChannel;

public class RoomsMessageHandler implements MessageHandler{

    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        Client client = message.getClient();
        SocketChannel clientChannel = client.getClientChannel();

        JsonObject response = new JsonObject();

        JsonArray roomsArray = new JsonArray();
        response.addProperty("type", "SCRoomsResult");
        for (ChatRoom room : commandProcessor.getChatRooms()) {
            JsonObject roomInfo = new JsonObject();
            roomInfo.addProperty("roomId", room.getId());
            roomInfo.addProperty("title", room.getName());
            JsonArray clientsArray = new JsonArray();
            for (Client roomClient : room.getClients()) {
                clientsArray.add(roomClient.getClientState().getNickname());
            }
            roomInfo.add("members", clientsArray);
            roomsArray.add(roomInfo);
        }

        response.add("rooms", roomsArray);
        messageSender.sendJsonMessage(clientChannel, response);
    }
}
