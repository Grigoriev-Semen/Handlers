package ru.grigoriev;

import org.apache.hc.core5.http.NameValuePair;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Request {
    private final String method;
    private final String path;
    private final List<String> headers;
    private String body;
    private List<NameValuePair> queryParams;

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setQueryParams(List<NameValuePair> params) {
        this.queryParams = params;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String param) {
        return asStream(queryParams)
                .filter(par -> par.getName().equals(param))
                .map(NameValuePair::getValue)
                .collect(Collectors.joining(": "));
    }

    public static <T> Stream<T> asStream(final Collection<T> collection) {
        return Optional.ofNullable(collection).stream()
                .flatMap(Collection::stream);
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
                "headers:" + "\n" + header +
                "body: " + body + "\n" +
                "queryParam: " + "\n" + queryParams;
    }
}