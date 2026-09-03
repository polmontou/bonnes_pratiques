package org.example;

public interface HandlersNotifier {
    void sendToOther(String message, ClientHandler sender);
}
