package org.example.client;

import java.nio.channels.SocketChannel;

public class Client {
    private final SocketChannel clientChannel;
    private final ClientState clientState;
    private boolean isProcessing;

    public Client(SocketChannel clientChannel, ClientState clientState) {
        this.clientChannel = clientChannel;
        this.clientState = clientState;
        this.isProcessing = false;
    }

    public SocketChannel getClientChannel() {
        return clientChannel;
    }

    public ClientState getClientState() {
        return clientState;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }
}
