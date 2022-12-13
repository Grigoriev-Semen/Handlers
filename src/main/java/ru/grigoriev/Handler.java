package ru.grigoriev;

import ru.grigoriev.Util.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Handler implements Runnable {
    private final Connection connection;

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Handler(Connection connection){
        this.connection = connection;
    }

    @Override
    public void run() {

        while (true) {
            try {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = connection.receive();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
                    continue;
                }

                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    connection.send((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ));
                    continue;
                }

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    );
                    connection.send(
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.getBytes().length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ,content);
                    continue;
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

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
