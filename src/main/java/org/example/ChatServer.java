package org.example;

import org.example.client.Client;
import org.example.client.ClientState;
import org.example.client.WorkerThread;
import org.example.message.CommandProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
public class ChatServer {
    private final int port;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private final ExecutorService executorService;
    private final List<Client> clients;
    private final Queue<Client> jobQueue;
    private final Lock queueLock;
    private final Condition newJobAvailable;
    private final CommandProcessor commandProcessor;
    private final Logger logger;

    public ChatServer(int port, int numberOfThreads) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.jobQueue = new LinkedBlockingQueue<>();
        this.clients = new CopyOnWriteArrayList<>();
        this.queueLock = new ReentrantLock();
        this.newJobAvailable = queueLock.newCondition();
        this.commandProcessor = new CommandProcessor(this);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(new WorkerThread(commandProcessor,jobQueue, queueLock, newJobAvailable));
        }
        logger=Logger.getLogger(ChatServer.class.getName());
    }

    public void start() {
        if (!initializeServer()) return;

        processSelectionKeys();
    }

    private boolean initializeServer() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "서버 초기화 중 오류 발생", e);
            return false;
        }
    }
    private void processSelectionKeys() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (selector.select() == 0) {
                    continue;
                }
                processSelectedKeys();
            }
        } catch (ClosedSelectorException e) {
            logger.log(Level.WARNING, "셀렉터가 닫혔습니다.");
        } catch (IOException e) {
            logger.severe(e.getMessage());
        } finally {
            stop();
        }
    }

    private void processSelectedKeys() throws IOException {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iter = selectedKeys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove();

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                accept(key);
            }

            synchronized (clients) {
                for (Client client : clients) {
                    if (client.getClientChannel().equals(key.channel()) && key.isReadable()) {
                        if (!client.isProcessing()) {
                            client.setProcessing(true); // 상태를 '처리 중'으로 설정
                            addJob(client); // 작업 큐에 추가
                        }
                    }
                }
            }


        }
    }

    public void addJob(Client client) {
        queueLock.lock();
        try {
            jobQueue.add(client);
            newJobAvailable.signal(); // 대기 중인 쓰레드에게 작업이 추가됨을 알림
        } finally {
            queueLock.unlock();
        }
    }



    private void accept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);

            String clientId = generateClientId(clientChannel);
            ClientState clientState = new ClientState(clientId);
            Client client = new Client(clientChannel, clientState);
            clientChannel.register(selector, SelectionKey.OP_READ, client);
            clients.add(client);
        }
    }

    private String generateClientId(SocketChannel channel) {
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            return remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
        } catch (IOException e) {
            return "Unknown";
        }
    }

    public Client getClient(String clientId) {
        synchronized (clients) {
            for (Client client : clients) {
                if (client.getClientState().getClientId().equals(clientId)) {
                    return client;
                }
            }
        }
        return null;
    }

    public synchronized void stop() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "서버를 종료하는 중에 오류 발생", e);
        }
    }
}

