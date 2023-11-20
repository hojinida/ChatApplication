package org.example.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ChatRoom {
    private final Long id;
    private final String name;
    private final List<Client> clients;
    private final Lock lock = new ReentrantLock();



    public ChatRoom(Long id, String name) {
        this.id = id;
        this.name = name;
        this.clients = new ArrayList<>();
    }

    public void addClient(Client client) {
        lock.lock();
        try {
            clients.add(client);
        } finally {
            lock.unlock();
        }
    }

    public void removeClient(Client client) {
        lock.lock();
        try {
            clients.remove(client);
        } finally {
            lock.unlock();
        }
    }

    public List<String> getClientIds() {
        lock.lock();
        try {
            return clients.stream()
                    .map(client -> client.getClientState().getClientId())
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public Long getId() {
        return id;
    }

    public List<Client> getClients() {
        return clients;
    }

    public String getName() {
        return name;
    }
}
