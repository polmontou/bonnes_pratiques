package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private final String address;
    private final int port;
    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    private BufferedReader console;
    private String username = null;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void start() {
        try {
            connect();
            startListening();
            readAndSendConsoleInput();
        } catch (IOException ex) {
            System.out.println("Connexion impossible : " + ex.getMessage());
        } finally {
            close();
        }
    }

    private void connect() throws IOException {
        socket = new Socket(address, port);
        socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        console = new BufferedReader(new InputStreamReader(System.in));
    }

    // Thread démon : n'empêche jamais le programme de se fermer, même s'il reste
    // bloqué sur la lecture du socket au moment où l'utilisateur quitte.
    private void startListening() {
        Thread listener = new Thread(this::listenToServer);
        listener.setDaemon(true);
        listener.start();
    }

    private void listenToServer() {
        try {
            String line;
            while ((line = socketReader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Connexion au serveur fermée.");
        } catch (IOException ex) {
            System.out.println("Connexion au serveur perdue.");
        } finally {
            System.exit(0);
        }
    }

    // Boucle principale : tant que l'utilisateur tape quelque chose, on envoie.
    // C'est ce thread qui pilote la fin du programme (quand la console se ferme
    // ou que l'utilisateur tape /quit).
    private void readAndSendConsoleInput() throws IOException {
        String line = console.readLine();
        if (line == null) {
            return;
        }
        username = line;
        socketWriter.println(line);

        System.out.print(username +": ");
        while ((line = console.readLine()) != null) {
            if (line.equalsIgnoreCase("/quit")) {
                break;
            }
            socketWriter.println(line);
            System.out.print(username +": ");
        }
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.out.println("Erreur à la fermeture : " + ex.getMessage());
        }
    }
}