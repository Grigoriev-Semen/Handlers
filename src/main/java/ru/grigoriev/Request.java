package ru.grigoriev;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private String body;
    private List<NameValuePair> queryParams;
    private final List<PostParam> postParams;

    public Request(String method, String path, List<String> listHeaders) {
        this.method = method;
        this.path = path;
        this.headers = new TreeMap<>();
        this.postParams = new ArrayList<>();
        this.queryParams = new ArrayList<>();
        listHeaders.forEach(v -> headers.put
                (StringUtils.substringBefore(v, " "), StringUtils.substringAfterLast(v, " ")));
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

    public Map<String, String> getHeaders() {
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
        if (!queryParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : param) {
                queryParams.stream()
                        .filter(par -> par.getName().equals(str))
                        .forEach(value -> sb.append(str).append("=").append(value.getValue()).append("\n"));
            }
            return sb.toString().isEmpty() ? "No result" : sb.toString();
        }
        return "No result";
    }

    public List<PostParam> getPostParams() {
        return postParams;
    }

    public String getPostParam(String... param) {
        if (!postParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : param) {
                postParams.stream()
                        .filter(par -> par.getName().equals(str))
                        .forEach(value -> sb.append(str).append("=").append(value.getValue()).append("\n"));
            }
            return sb.toString().isEmpty() ? "No result" : sb.toString();
        }
        return "No result";
    }

    public void addPostParam(PostParam postParam) {
        postParams.add(postParam);
    }

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            header.append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
        }
        return "Request:" + "\n" +
                "method = " + method + "\n" +
                "path = " + path + "\n" +
                "headers:" + "\n" + header +
                "body: " + "\n" + body + "\n";
    }

    static class PostParam {
        private final String name;
        private final String value;

        public PostParam(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + "=" + value + "\n";
        }
    }
}