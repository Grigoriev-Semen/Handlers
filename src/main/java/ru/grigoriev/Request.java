package ru.grigoriev;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.NameValuePair;

import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private String body;
    private List<NameValuePair> queryParams;
    private final Map<String,List<PostParam>>  postParams;

    public Request(String method, String path, List<String> listHeaders) {
        this.method = method;
        this.path = path;
        this.headers = new TreeMap<>();
        this.postParams = new HashMap<>();
        listHeaders.forEach(v -> headers.put
                (StringUtils.substringBefore(v, " "), StringUtils.substringAfterLast(v," ")));
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
        if (queryParams != null || !queryParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : param) {
                queryParams.forEach(v-> sb.append(v).append("\n"));
            }
            return sb.toString();
        }
        return "No result";
    }

    public Map<String,List<PostParam>> getPostParam() {
        return postParams;
    }

    public String getPostParam(String... param) {
        if (postParams != null || !postParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : param) {
                for (PostParam postParam : postParams.get(str)) {
                    sb.append(postParam);
                }
            }
            return sb.toString();
        }
        return "No result";
    }

    public void addPostParam(String key,PostParam postParam){
        postParams.computeIfPresent(key, (k, v) -> {
            v.add(postParam);
            return v;
        });
        postParams.putIfAbsent(key, List.of(postParam));
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

    static  class PostParam {
        private String name;
        private String value;

        public PostParam(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return  name + "=" + value + "\n";
        }
    }
}