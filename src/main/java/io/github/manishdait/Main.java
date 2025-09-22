package io.github.manishdait;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) throws IOException {
    ServerSocket server = new ServerSocket(6379);

    while (true) {
      Socket socket = server.accept();

      Object object = new RespDeserializer(socket.getInputStream()).decode();
  

      if (object instanceof Object[]) {
        Object[] cmd =  (Object[]) object;

        for (Object obj : cmd) { 
          System.out.println(obj);
        }

        if (cmd[0].equals("PING")) {
          socket.getOutputStream().write(RespSerializer.toSimpleString("PONG").getBytes(StandardCharsets.UTF_8));
        }
      }

      socket.close();
    }
  }
}