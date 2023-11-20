package org.example;

import static java.lang.System.exit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("인자로 스레드풀 개수를 입력하세요");
            exit(1);
        }

        int port = 9166;
        int numberOfThreads = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port,numberOfThreads);
        chatServer.start();
    }
}