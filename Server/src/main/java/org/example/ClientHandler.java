package org.example;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private Socket clientSocket;
    public BufferedReader inputReader;
    public PrintWriter outputWriter;
    public String username;

    public ClientHandler (Socket socket){
        clientSocket = socket;
        try {
            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void run() {
        try {
            register();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void register() throws IOException {
        outputWriter.println("Enter your name : ");
        username = inputReader.readLine();
    }
}
