package ru.grigoriev;

import ru.grigoriev.Util.Connection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Connection connection;

    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public ClientHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Request request = createRequest();

                Handler handler = Server.getHandlers().get(request.getMethod()).get(request.getPath());

                if (handler == null) {
                    Path parent = Path.of(request.getPath()).getParent();
                    handler = Server.getHandlers().get(request.getMethod()).get(parent.toString());
                    if (handler == null) {
                        error404();
                        return;
                    }
                }

                handler.handle(request, connection);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Request createRequest() throws Exception {
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = connection.receive();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            connection.close();
        }
        final var method = parts[0];
        final var path = parts[1];
        if (!validPaths.contains(path)) {
            error404();
            connection.close();
        }

        List<String> headers = new ArrayList<>();
        String str;
        while (!(str = connection.receive()).equals("")) {
            headers.add(str);
        }

        var request = new Request(method, path, connection.getSocket().getInputStream(), headers);
        System.out.println(request);
        return request;
    }

    private void error404() throws IOException {
        connection.send((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ));
    }
}
