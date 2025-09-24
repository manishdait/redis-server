package io.github.manishdait.redis_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final Map<String, Object> MAP = new HashMap<>();

  public static void main(String[] args) throws IOException {
    int PORT = 6379;
    ServerSocket server = new ServerSocket(PORT);
    logger.info("Server started at port " + PORT);

    while (true) {
      Socket socket = server.accept();
      new Thread(new SocketHandler(socket)).start();
    }
  }

  public static Map<String, Object> getMap() {
    return Main.MAP;
  }
}