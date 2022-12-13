package ru.grigoriev;

import java.io.*;
import java.util.Properties;

public class Main {
  public static void main(String[] args) {
    int port = 0;
    int countConnection = 0;
    var property = new Properties();

    try {
      property.load(new FileInputStream("src/main/resources/settings.properties"));
      port = Integer.parseInt(property.getProperty("port"));
      countConnection = Integer.parseInt(property.getProperty("countConnection"));
    } catch (IOException e) {

      throw new RuntimeException(e);
    }

    new Server(port,countConnection).start();
  }
}