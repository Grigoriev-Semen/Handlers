package ru.grigoriev.Util;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Connection implements Closeable {
    private final Socket socket;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    public Connection(Socket socket) throws Exception {
        this.socket = socket;
        this.out = new BufferedOutputStream(socket.getOutputStream());
        this.in = new BufferedInputStream
                (socket.getInputStream());
    }

    public void send(String... message) throws IOException {
        for (String s : message) {
            out.write(s.getBytes());
        }
        out.flush();
    }

    public void send(Path path) throws IOException {
        Files.copy(path, out);
        out.flush();
    }

    public int read(byte[] buffer) throws IOException {
        return in.read(buffer);
    }

    public BufferedInputStream getIn() {
        return in;
    }

    public BufferedOutputStream getOut() {
        return out;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
