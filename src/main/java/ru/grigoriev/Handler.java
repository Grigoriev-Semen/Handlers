package ru.grigoriev;

import ru.grigoriev.Util.Connection;

@FunctionalInterface
public interface Handler {
    void handle(Request request, Connection connection);
}
