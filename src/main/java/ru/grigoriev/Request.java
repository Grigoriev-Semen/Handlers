package ru.grigoriev;

import java.io.InputStream;
import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final InputStream body;
    private final List<String> headers;

    public Request(String method, String path, InputStream body, List<String> headers) {
        this.method = method;
        this.path = path;
        this.body = body;
        this.headers = headers;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder();
        for (String s : headers) {
            header.append(s).append("\n");
        }
        return "Request:" + "\n" +
                "method = " + method + "\n" +
                "path = " + path + "\n" +
                "headers:" + "\n" + header;
    }
}