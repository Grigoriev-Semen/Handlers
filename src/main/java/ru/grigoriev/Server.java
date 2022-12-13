package ru.grigoriev;

import ru.grigoriev.Util.Connection;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private ExecutorService executorService;

    public Server(int port, int countConnection) {
        this.port = port;
        executorService = Executors.newFixedThreadPool(countConnection);
    }

    public void start(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                var socket = serverSocket.accept();
                executorService.submit(new Handler(new Connection(socket)));
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }
}

