package ru.grigoriev;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URLEncodedUtils;
import ru.grigoriev.Util.Connection;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final Connection connection;

    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/default-get.html");

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

                System.out.println(request);

                System.out.println("\n-------Print param----------");
                System.out.println(request.getQueryParam("value", "name"));

                System.out.println("\n-------Print postParam----------");
                System.out.println(request.getPostParam("value","title"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Request createRequest() throws Exception {
        // лимит на request line + заголовки
        final int limit = 4096;

        connection.getIn().mark(limit);
        final byte[] buffer = new byte[limit];

        final int read = connection.read(buffer);
        // ищем request line

        final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            error404();
            connection.close();
            return null;
        }
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        if (requestLine.length != 3) {
            error404();
            connection.close();
            return null;
        }

        final var method = requestLine[0];
        final var path = requestLine[1];
        if (!requestLine[1].startsWith("/")) {
            error404();
            connection.close();
            return null;
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            error404();
            connection.close();
            return null;
        }

        // отматываем на начало буфера
        connection.getIn().reset();
        // пропускаем requestLine
        connection.getIn().skip(headersStart);

        final var headersBytes = connection.getIn().readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        Request request = new Request(method, path, headers);

        // для GET тела нет
        if (!method.equals("GET")) {
            connection.getIn().skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var content = extractHeader(headers, "Content-Length");
            if (content.isPresent()) {
                final var length = Integer.parseInt(content.get());
                final var bodyBytes = connection.getIn().readNBytes(length);
                request.setBody(new String(bodyBytes));
            }
            final URI uri = new URI(path);
            request.setQueryParams(URLEncodedUtils.parse(uri, StandardCharsets.UTF_8));
            // делаем проверку на enctype="application/x-www-form-urlencoded"
            // если "да", то собираем параметры
             if(request.getHeaders().get("Content-Type:").equals("application/x-www-form-urlencoded")){
                 for (String str : request.getBody().split("&")) {
                     request.addPostParam(str, new Request.PostParam
                             (StringUtils.substringBefore(str, "="), StringUtils.substringAfter(str, "=")));
                 }
             }
        }

        return request;
    }

    static void response(Request request, Connection connection) {
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
    }

    private void error404() throws IOException {
        connection.send((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ));
    }

    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
