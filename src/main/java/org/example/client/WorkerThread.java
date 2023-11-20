package org.example.client;

import org.example.message.CommandProcessor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkerThread implements Runnable {
    private final CommandProcessor commandProcessor;
    private final Queue<Client> jobQueue;
    private final Lock queueLock;
    private final Condition newJobAvailable;
    private final Logger logger;

    public WorkerThread(CommandProcessor commandProcessor, Queue<Client> jobQueue,Lock queueLock, Condition newJobAvailable) {
        this.commandProcessor = commandProcessor;
        this.jobQueue = jobQueue;
        this.queueLock= queueLock;
        this.newJobAvailable = newJobAvailable;
        this.logger = Logger.getLogger(WorkerThread.class.getName());
    }

    @Override
    public void run() {
        while (true) {
            Client client= null;
            String message = null;
            try {
                queueLock.lock();
                try {
                    while (jobQueue.isEmpty()) {
                        newJobAvailable.await();
                    }
                    client = jobQueue.remove();
                    message=processClient(client);
                    client.setProcessing(false);
                } finally {
                    queueLock.unlock();
                }
                processCommand(client, message);
            } catch (InterruptedException e) {
                break; // 쓰레드 중단 처리
            }
        }
    }

    private void processCommand(Client client, String message){
        commandProcessor.processMessage(client,message);
    }

    private String processClient(Client client){
        String message=null;
        ByteBuffer buffer = ByteBuffer.allocate(2); // 길이를 읽기 위한 버퍼
        try {
            int length = readMessageLength(buffer,client);
            message = readMessage(length,client);
        }catch (IOException e){
            logger.log(Level.WARNING, "클라이언트 메시지 읽기 오류", e);
        }
        return message;
    }

    private int readMessageLength(ByteBuffer lengthBuffer,Client client) throws IOException {
        while (lengthBuffer.hasRemaining()) {
            int bytesRead = client.getClientChannel().read(lengthBuffer);
            if (bytesRead < 0) return -1;
        }
        lengthBuffer.flip();

        return lengthBuffer.getShort() & 0xffff;
    }

    private String readMessage(int expectedLength,Client client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        while (buffer.hasRemaining()) {
            int bytesRead = client.getClientChannel().read(buffer);
            if (bytesRead < 0) return null;
        }
        buffer.flip();
        String result = new String(buffer.array(), StandardCharsets.UTF_8).trim();
        buffer.clear();
        return result;
    }
}

