package ru.grigoriev;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        int port = 0;
        int countConnection = 0;
        var property = new Properties();

        try {
            property.load(new FileInputStream("src/main/resources/settings.properties"));
            port = Integer.parseInt(property.getProperty("port"));
            countConnection = Integer.parseInt(property.getProperty("countConnection"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final var server = new Server(port, countConnection);

        for (String validPath : ClientHandler.validPaths) {
            server.addHandler("GET", validPath, (request, connection) -> {
                ClientHandler.response(request, connection);
            });
        }

        server.addHandler("POST", "/default-get.html", (request, connection) -> {
            ClientHandler.response(request, connection);
        });

        server.start();
    }
}