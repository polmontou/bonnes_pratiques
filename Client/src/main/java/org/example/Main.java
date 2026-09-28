package org.example;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

// Main du client
public class Main {
    public static void main(String[] args) {
        String address = "localhost";
        int port = 12345;
        Client c = new Client(address, port);

        c.start();
    }
}