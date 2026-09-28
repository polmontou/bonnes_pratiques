package org.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import static java.lang.Integer.parseInt;

public class Main {
    public static void main(String[] args) {
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        Path parentPath = Paths.get(path).getParent().getParent().getParent().getParent();
        String appConfigPath = parentPath + "/app.properties";
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(appConfigPath));
            Server s = new Server(parseInt(appProps.getProperty("port")));
            s.start();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}