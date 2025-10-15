package io.github.manishdait.redis_server;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);
  private static final Charset UTF_8 = StandardCharsets.UTF_8;
  private final Socket socket;

  public SocketHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    logger.info("Client connected: {}", socket.getRemoteSocketAddress());
    try {
      RespDeserializer deserializer = (RespDeserializer) new RespDeserializer(socket.getInputStream());

      while(true) {
        Object[] args = (Object[]) deserializer.decode();
        String command = ((String) args[0]).toUpperCase();

        logger.debug("Processing command: {} | Args: {}", command, Arrays.toString(args));

        switch (command) {
          case "PING": 
            returnSimple("PONG");
            break;
          case "ECHO":
            returnSimple((String) args[1]);
            break;
          case "SET":
            set(args);
            break;
          case "GET":
            get((String) args[1]);
            break;
          case "INCR":
            incr((String) args[1]);
            break;
          case "DECR":
            decr((String) args[1]);
            break;
          case "COMMAND":
            returnSimple("");
            break;
          default:
            logger.warn("Invalid command received: {}", command);
            returnError("Invalid command " + command);
        }
      }

    } catch (Exception e) {
      logger.error("Error while handling client {}", socket.getRemoteSocketAddress(), e);
    }
  }

  private void set(Object[] args) throws IOException { 
    String key = (String) args[1];
    String value = (String) args[2];

    Entity entity = new Entity(key, value, null);

    for (int i = 3; i < args.length; i++) {
      if (((String) args[i]).toLowerCase().equals("NX".toLowerCase())) {
        if (Main.getMap().containsKey(key)) {
          socket.getOutputStream().write("$-1\r\n".getBytes(UTF_8));
          return;
        }
      }

      else if (((String) args[i]).toLowerCase().equals("XX".toLowerCase())) {
        if (!Main.getMap().containsKey(key)) {
          socket.getOutputStream().write("$-1\r\n".getBytes(UTF_8));
          return;
        }
      }

      else if (((String)args[i]).toLowerCase().equals("EX".toLowerCase())) {
        i++;
        entity.setExpiration(Instant.now().plusSeconds(Integer.parseInt((String) args[i])));
      }

      else if (((String)args[i]).toLowerCase().equals("PX".toLowerCase())) {
        i++;
        entity.setExpiration(Instant.now().plusMillis(Integer.parseInt((String) args[i])));
      }

      else if (((String)args[i]).toLowerCase().equals("EXAT".toLowerCase())) {
        i++;
        entity.setExpiration(Instant.ofEpochSecond(Long.parseLong((String) args[i])));
      }

      else if (((String)args[i]).toLowerCase().equals("PX".toLowerCase())) {
        i++;
        entity.setExpiration(Instant.ofEpochMilli(Long.parseLong((String) args[i])));
      }
    }

    Main.getMap().put(key, entity);
    logger.debug("SET {} = {}", key, entity);

    returnSimple("OK");
  }

  private void get(String key) throws IOException {
    if (!exists(key)) {
      socket.getOutputStream().write("$-1\r\n".getBytes(UTF_8));
      logger.debug("GET {} -> (nil)", key);

      return;
    }

    Entity value = Main.getMap().get(key);
    
    if (value.getValue() instanceof Object[]) {
      returnArray((Object[]) value.getValue());
    } else {
      returnBulk((String) value.getValue());
    }

    logger.debug("GET {} -> {}", key, value);
  }

  private void incr(String key) throws IOException {
     if (!exists(key)) {
      socket.getOutputStream().write("$-1\r\n".getBytes(UTF_8));
      return;
    }

    Long val = (Long.parseLong((String) Main.getMap().get(key).getValue())) + 1;
    Main.getMap().get(key).setValue(String.valueOf(val));
    
    returnInteger(val);
    logger.debug("INCR {} -> {}", key, val);
  }

  private void decr(String key) throws IOException {
     if (!exists(key)) {
      socket.getOutputStream().write("$-1\r\n".getBytes(UTF_8));
      return;
    }

    Long val = (Long.parseLong((String) Main.getMap().get(key).getValue())) - 1;
    Main.getMap().get(key).setValue(String.valueOf(val));
    
    returnInteger(val);
    logger.debug("DECR {} -> {}", key, val);
  }

  private boolean exists(String key){
    return Main.getMap().containsKey(key);
  }

  private void returnSimple(String str) throws IOException {
    socket.getOutputStream().write(RespSerializer.toSimpleString(str).getBytes(UTF_8));
    logger.trace("Sent Simple String: {}", str);
  }

  private void returnBulk(String str) throws IOException {
    socket.getOutputStream().write(RespSerializer.toBulkString(str).getBytes(UTF_8));
    logger.trace("Sent Bulk String: {}", str);
  }

  private void returnInteger(long value) throws IOException {
    socket.getOutputStream().write(RespSerializer.toInteger(value).getBytes(UTF_8));
    logger.trace("Sent Integer: {}", value);
  }

  private void returnArray(Object[] arr) throws IOException {
    socket.getOutputStream().write(RespSerializer.toArray(arr).getBytes(UTF_8));
    logger.trace("Sent Array: {}", Arrays.toString(arr));
  }

  private void returnError(String err) throws IOException {
    socket.getOutputStream().write(RespSerializer.toError(err).getBytes(UTF_8));
    logger.trace("Sent Error: {}", err);
  }
}
