package io.github.manishdait.redis_server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.manishdait.redis_server.types.RMap;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static RMap MAP;

  public static void main(String[] args) throws IOException {
    int PORT = 6379;
    ServerSocket server = new ServerSocket(PORT);
    logger.info("Server started at port " + PORT);

    loadInitData();

    while (true) {
      Socket socket = server.accept();
      new Thread(new SocketHandler(socket)).start();
    }
  }

  public static RMap getMap() {
    return Main.MAP;
  }

  public static void loadInitData(){
    try {
      FileInputStream fileStream = new FileInputStream("dump");
      ObjectInputStream in = new ObjectInputStream(fileStream);

      MAP = (RMap) in.readObject();
      in.close();

      logger.info("Data import successfull");
    } catch (Exception e) {
      e.printStackTrace();
      MAP = new RMap();
    }
  }
}