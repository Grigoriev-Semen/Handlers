package ru.grigoriev;

import org.apache.hc.core5.http.NameValuePair;

import java.util.List;

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
        if (path.contains("?")) {
            int index = path.indexOf("?");
            return path.substring(0, index);
        }
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

    public String getQueryParam(String... param) {
        if (queryParams != null || !queryParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : param) {
                queryParams.stream()
                        .filter(par -> par.getName().equals(str))
                        .forEach(value -> sb.append(str).append("=").append(value.getValue()).append("\n"));
            }
            return sb.toString();
        }
        return "No result";
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
                "body: " + "\n" + body + "\n";
    }
}