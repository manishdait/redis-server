package io.github.manishdait;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketHandler implements Runnable {
  private final Socket socket;

  public SocketHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      RespDeserializer deserializer = (RespDeserializer) new RespDeserializer(socket.getInputStream());

      while(true) {
        Object[] args = (Object[]) deserializer.decode();
        String command = ((String) args[0]).toUpperCase();

        switch (command) {
          case "PING": 
            returnResponse("PONG");
            break;
          case "ECHO":
            returnResponse((String) args[1]);
            break;
          case "SET":
            set((String) args[1], args[2]);
            break;
          case "GET":
            get((String) args[1]);
            break;
          default:
            returnResponse("");
        }
      }


    } catch (Exception e) {

    }
  }

  private void set(String key, Object value) throws IOException { 
    Main.MAP.put(key, value);
    returnResponse("OK");
  }


  private void get(String key) throws IOException {
    if (!Main.MAP.containsKey(key)) {
      socket.getOutputStream().write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
      return;
    }
    
    Object value = Main.MAP.get(key);

    if (value instanceof String) {
      socket.getOutputStream().write(RespSerializer.toBulkString((String) value).getBytes(StandardCharsets.UTF_8));
      return;
    }

    if (value instanceof Number) {
      System.out.println(((Number)value).longValue());
      socket.getOutputStream().write(RespSerializer.toInteger(((Number)value).longValue()).getBytes(StandardCharsets.UTF_8));
      return;
    }

    if (value instanceof Object[]) {
      socket.getOutputStream().write(RespSerializer.toArray((Object[]) value).getBytes(StandardCharsets.UTF_8));
      return;
    }
  }

  private void returnResponse(String str) throws IOException {
    socket.getOutputStream().write(RespSerializer.toSimpleString(str).getBytes(StandardCharsets.UTF_8));
  }
}
