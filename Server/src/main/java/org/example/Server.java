package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements HandlersNotifier {
    private int port;
    private Map<UUID, ClientHandler> clientsList = Collections.synchronizedMap(new HashMap<>());
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private List<Message> chatHistory = Collections.synchronizedList(new ArrayList<>());

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        isRunning = true;
        System.out.println("Chat server started on port " + port);

        while (isRunning) {
            Socket cs = serverSocket.accept();
            ClientHandler newClient = new ClientHandler(cs, this);

            // Le client n'est PAS ajouté à clientsList ici : c'est register()
            // (dans son propre thread) qui appelle addToClientsList une fois le pseudo saisi.
            Thread t = new Thread(newClient);
            t.start();
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    public void addToClientsList(ClientHandler client) {
        clientsList.put(client.id, client);
    }

    public void sendToOthers(ClientHandler sender, String message) {
        Message newMessage = new Message(sender.username, message);
        addToChatHistory(newMessage);

        List<ClientHandler> clientsListCopy;

        // Itération sur une synchronizedMap : verrou manuel sur la map pendant la copie
        synchronized (clientsList) {
            clientsListCopy = new ArrayList<>(clientsList.values());
        }

        for (ClientHandler client : clientsListCopy) {
            if (client != null && !Objects.equals(client.id, sender.id)) {
                client.receiveMessage(newMessage);
            }
        }
    }

    public void getChatHistory(ClientHandler client) {
        List<Message> chatHistoryCopy;
        synchronized (chatHistory) {
            chatHistoryCopy = new ArrayList<Message>(chatHistory);
        }
        for (Message message : chatHistoryCopy) {
            client.receiveMessage(message);
        }
    }

    public void notifyDisconnectionOf(ClientHandler client) {
        clientsList.remove(client.id);
        if (client.username != null) {
            sendToOthers(client, " has left the chat!");
        }
    }

    public void notifyConnectionOf(ClientHandler client) {
        if (client.username != null) {
            sendToOthers(client, " has join the chat!");
        }
    }

    private void addToChatHistory(Message message) {
        synchronized (chatHistory) {
            chatHistory.add(message);
            if (chatHistory.size() > 100) {
                chatHistory.remove(0);
            }
        }
    }

    public void getNotifiedFrom(ClientHandler client, String message) {
        System.out.println(client + " : " + message);
    }
}
