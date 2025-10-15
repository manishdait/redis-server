package io.github.manishdait.redis_server;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);
  private final Socket socket;
  
  private static final Charset UTF_8 = StandardCharsets.UTF_8;
  private static final byte[] NIL = "$-1\r\n".getBytes(UTF_8);
  private static final Map<String, Entity> MAP = Main.getMap();

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
        String command = toString(args[0]).toUpperCase();

        logger.debug("Processing command: {} | Args: {}", command, Arrays.toString(args));

        switch (command) {
          case "PING": 
            returnSimple("PONG");
            break;
          case "ECHO":
            returnSimple(toString(args[1]));
            break;
          case "SET":
            set(args);
            break;
          case "GET":
            get(toString(args[1]));
            break;
          case "INCR":
            incr(toString(args[1]));
            break;
          case "DECR":
            decr(toString(args[1]));
            break;
          case "EXISTS":
            exists(args);
            break;
          case "DEL":
            del(args);
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
    String key = toString(args[1]);
    String value = toString(args[2]);

    Entity entity = new Entity(key, value, null);

    for (int i = 3; i < args.length; i++) {
      String arg = toString(args[i]);

      if (arg.toUpperCase().equals("NX")) {
        if (MAP.containsKey(key)) {
          socket.getOutputStream().write(NIL);
          return;
        }
      }

      else if (arg.toUpperCase().equals("XX")) {
        if (!MAP.containsKey(key)) {
          socket.getOutputStream().write(NIL);
          return;
        }
      }

      else if (arg.toUpperCase().equals("EX")) {
        i++;
        entity.setExpiration(Instant.now().plusSeconds(toInt(args[i])));
      }

      else if (arg.toUpperCase().equals("PX")) {
        i++;
        entity.setExpiration(Instant.now().plusMillis(toInt(args[i])));
      }

      else if (arg.toUpperCase().equals("EXAT")) {
        i++;
        entity.setExpiration(Instant.ofEpochSecond(toLong(args[i])));
      }

      else if (arg.toUpperCase().equals("PX")) {
        i++;
        entity.setExpiration(Instant.ofEpochMilli(toLong(args[i])));
      }
    }

    MAP.put(key, entity);
    logger.debug("SET {} = {}", key, entity);

    returnSimple("OK");
  }

  private void get(String key) throws IOException {
    if (!exists(key)) {
      socket.getOutputStream().write(NIL);
      logger.debug("GET {} -> (nil)", key);

      return;
    }

    Entity value = MAP.get(key);
    
    if (value.getValue() instanceof Object[]) {
      returnArray((Object[]) value.getValue());
    } else {
      returnBulk((String) value.getValue());
    }

    logger.debug("GET {} -> {}", key, value);
  }

  private void incr(String key) throws IOException {
     if (!exists(key)) {
      socket.getOutputStream().write(NIL);
      return;
    }

    Long val = toLong(MAP.get(key).getValue()) + 1;
    MAP.get(key).setValue(String.valueOf(val));
    
    returnInteger(val);
    logger.debug("INCR {} -> {}", key, val);
  }

  private void decr(String key) throws IOException {
     if (!exists(key)) {
      socket.getOutputStream().write(NIL);
      return;
    }

    Long val = toLong(MAP.get(key)) - 1;
    MAP.get(key).setValue(String.valueOf(val));
    
    returnInteger(val);
    logger.debug("DECR {} -> {}", key, val);
  }

  private void exists(Object[] args) throws IOException{
    int count = 0;

    for (int i = 1; i < args.length; i++) {
      String key = toString(args[i]);
      if (MAP.containsKey(key)) {
        count++;
      }
    }

    returnInteger(count);
  }

  private void del(Object[] args) throws IOException{
    int count = 0;

    for (int i = 1; i < args.length; i++) {
      String key = toString(args[i]);
      if (MAP.containsKey(key)) {
        MAP.remove(key);
        count++;
      }
    }

    returnInteger(count);
  }

  private boolean exists(String key){
    return MAP.containsKey(key);
  }

  private String toString(Object obj) {
    return (String) obj;
  }

  private Integer toInt(Object obj) {
    return Integer.parseInt(toString(obj));
  }

  private Long toLong(Object obj) {
    return Long.parseLong(toString(obj));
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
