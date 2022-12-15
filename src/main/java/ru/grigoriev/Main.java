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
                try {
                    final var filePath = Path.of(".", "public", request.getPath());
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    final var template = Files.readString(filePath);
                    if (template.contains("{time}")) {
                        final var content = template.replace("{time}", LocalDateTime.now().toString());
                        connection.send(
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.getBytes().length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                                , content);
                    }


                    final var length = Files.size(filePath);
                    connection.send(
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    );
                    connection.send(filePath);
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        server.addHandler("POST", "/messages", (request, connection) -> {
        });

        server.start();
    }
}