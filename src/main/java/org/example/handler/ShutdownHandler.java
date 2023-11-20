package org.example.handler;

import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

public class ShutdownHandler implements MessageHandler{
    @Override
    public void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message) {
        commandProcessor.shutdownServer();
    }
}
