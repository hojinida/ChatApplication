package org.example.message;

import com.google.gson.JsonObject;
import org.example.client.Client;

public class Message {
    private final Client client;

    private final JsonObject jsonObject;

    public Message(Client client, JsonObject jsonObject) {
        this.client = client;
        this.jsonObject = jsonObject;
    }

    public Client getClient() {
        return client;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }
}
