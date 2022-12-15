package ru.grigoriev;

import ru.grigoriev.Util.Connection;

import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService executorService;
    private final static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port, int countConnection) {
        this.port = port;
        executorService = Executors.newFixedThreadPool(countConnection);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                var socket = serverSocket.accept();
                executorService.submit(new ClientHandler(new Connection(socket)));
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            handlers.put(method, new ConcurrentHashMap<>(Map.of(path, handler)));
        }
    }

    public static Map<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }
}

