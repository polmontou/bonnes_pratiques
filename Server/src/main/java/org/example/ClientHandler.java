package org.example;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    public BufferedReader inputReader;
    private PrintWriter outputWriter;
    public String username;
    public HandlersNotifier notifier;
    public UUID id;

    public ClientHandler(Socket socket, HandlersNotifier server) {
        clientSocket = socket;
        notifier = server;
        id = UUID.randomUUID();

        try {
            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
        } catch (IOException ex) {
            notifier.getNotifiedFrom(this, ex.getMessage());
        }
    }

    public void run() {
        try {
            register();
            listeningToClientMessages();
        } catch (IOException ex) {
            // Le catch se contente d'absorber/loguer l'exception (déconnexion brutale).
            // Le nettoyage se fait une seule fois, dans le finally, quel que soit le chemin de sortie.
            notifier.getNotifiedFrom(this, ex.getMessage());
        } finally {
            closeSession();
            notifier.notifyDisconnectionOf(this);
        }
    }

    private void register() throws IOException {
        outputWriter.println("Enter your name : ");
        username = inputReader.readLine();
        if (username != null) {
            outputWriter.println("Welcome to our super chat " + username + "!");
            // Ordre important : on envoie l'historique AVANT de notifier la connexion,
            // pour que le message "a rejoint le chat" ne soit pas encore dans chatHistory
            // au moment où on l'envoie à ce nouveau client (sinon il le recevrait deux fois).
            notifier.getChatHistory(this);
            notifier.notifyConnectionOf(this);
            // Le client n'est ajouté à clientsList qu'une fois son pseudo saisi,
            // pour qu'il ne puisse pas recevoir de message avant d'être enregistré.
            notifier.addToClientsList(this);
        }
    }

    public void receiveMessage(Message message) {
        outputWriter.println(message.sender + " : " + message.text);
    }

    public void getNotifiedFromServer(String message) {
        outputWriter.println("/// Server \\\\\\: " + message);
    }

    private void listeningToClientMessages() throws IOException {
        String incomingMessage;
        while ((incomingMessage = inputReader.readLine()) != null) {
            notifier.sendToOthers(this, incomingMessage, false);
        }
    }

    private void closeSession() {
        if (outputWriter != null) {
            outputWriter.close();
        }
        try {
            if (inputReader != null) {
                inputReader.close();
            }
        } catch (IOException ex) {
            notifier.getNotifiedFrom(this, ex.getMessage());
        }
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException ex) {
            notifier.getNotifiedFrom(this, ex.getMessage());
        }
    }
}