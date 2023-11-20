package org.example.handler;

import org.example.message.CommandProcessor;
import org.example.message.Message;
import org.example.message.MessageSender;

public interface MessageHandler {
    void handle(CommandProcessor commandProcessor, MessageSender messageSender, Message message);
}
