package io.github.manishdait;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {
  static final Map<String, Object> MAP = new HashMap<>();

  public static void main(String[] args) throws IOException {
    ServerSocket server = new ServerSocket(6379);

    while (true) {
      Socket socket = server.accept();
      new Thread(new SocketHandler(socket)).start();
    }
  }
}