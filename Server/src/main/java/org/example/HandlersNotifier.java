package org.example;

public interface HandlersNotifier {
    void sendToOthers(ClientHandler sender, String message);
    void notifyDisconnectionOf(ClientHandler client);
    void getNotifiedFrom(ClientHandler client, String message);
    void notifyConnectionOf(ClientHandler client);
    void getChatHistory(ClientHandler client);
    void addToClientsList(ClientHandler client);
}