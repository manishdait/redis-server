package io.github.manishdait.redis_server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

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

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        cleanExpiry();
      }
    };

    timer.scheduleAtFixedRate(timerTask, 0, 30000);

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
      MAP = new RMap();
    }
  }

  private static void cleanExpiry() {
    for (String key : MAP.keys()) {
      if (MAP.getString(key).hasExpire()) {
        MAP.remove(key);
        logger.debug("Cleaning expired key {}", key);;
      }
    }
  }
}
