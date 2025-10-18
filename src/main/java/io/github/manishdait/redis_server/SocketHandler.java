package io.github.manishdait.redis_server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.manishdait.redis_server.types.RList;
import io.github.manishdait.redis_server.types.RMap;
import io.github.manishdait.redis_server.types.RString;

public class SocketHandler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);
  
  private static final Charset UTF_8 = StandardCharsets.UTF_8;
  private static final RMap MAP = Main.getMap();
  
  private final Socket socket;

  public SocketHandler(Socket socket) {
    logger.info("Client connected: {}", socket.getRemoteSocketAddress());
    this.socket = socket;
  }

  @Override
  public void run() {
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
          case "LPUSH":
            lpush(args);
            break;
          case "RPUSH":
            rpush(args);
            break;
          case "LINDEX":
            lindex(args);
            break;
          case "LRANGE":
            lrange(args);
            break;
          case "SAVE":
            save();
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

    RString rString = new RString(toString(args[2]));

    for (int i = 3; i < args.length; i++) {
      String arg = toString(args[i]);

      if (arg.toUpperCase().equals("NX")) {
        if (MAP.contains(key)) {
          returnNil();
          return;
        }
      }

      else if (arg.toUpperCase().equals("XX")) {
        if (!MAP.contains(key)) {
          returnNil();
          return;
        }
      }

      else if (arg.toUpperCase().equals("EX")) {
        rString.plusSeconds(toLong(args[++i]));
      }

      else if (arg.toUpperCase().equals("PX")) {
        rString.plusMilliSeconds(toLong(args[++i]));
      }

      else if (arg.toUpperCase().equals("EXAT")) {
        rString.setExpirationSeconds(toLong(args[++i]));
      }

      else if (arg.toUpperCase().equals("PXAT")) {
        rString.setExpirationMills(toLong(args[++i]));
      }
    }

    MAP.put(key, rString);
    logger.debug("SET {} = {}", key, rString);

    returnSimple("OK");
  }

  private void get(String key) throws IOException {
    if (!exists(key)) {
      returnNil();
      logger.debug("GET {} -> (nil)", key);
      return;
    }

    try {
      RString rString = MAP.getString(key);
      returnBulk(rString.getValue());

      logger.debug("GET {} -> {}", key, rString);
    } catch (Exception e) {
      returnError(e.getMessage());
      logger.error("GET {} -> {}", key, e.getMessage());
    }
  }

  private void incr(String key) throws IOException {
    if (!exists(key)) {
      returnNil();
      logger.debug("INCR {} -> (nil)", key);
      return;
    }

    try {
      RString rString = MAP.getString(key);
      Long value = Long.parseLong(rString.getValue()) + 1;

      MAP.getString(key).setValue(String.valueOf(value));
      returnInteger(value);
      
      logger.debug("INCR {} -> {}", key, value);
    } catch (Exception e) {
      returnError(e.getMessage());
      logger.error("INCR {} -> {}", key, e.getMessage());
    }
  }

  private void decr(String key) throws IOException {
    if (!exists(key)) {
      returnNil();
      logger.debug("DECR {} -> (nil)", key);
      return;
    }

    try {
      RString rString = MAP.getString(key);
      Long value = Long.parseLong(rString.getValue()) - 1;

      MAP.getString(key).setValue(String.valueOf(value));
      returnInteger(value);
      
      logger.debug("DECR {} -> {}", key, value);
    } catch (Exception e) {
      returnError(e.getMessage());
      logger.error("DECR {} -> {}", key, e.getMessage());
    }
  }

  private void exists(Object[] args) throws IOException{
    int count = 0;

    for (int i = 1; i < args.length; i++) {
      String key = toString(args[i]);

      if (MAP.contains(key)) {
        count++;
      }
    }

    returnInteger(count);
    logger.debug("EXISTS -> {}", count);
  }

  private void del(Object[] args) throws IOException{
    int count = 0;

    for (int i = 1; i < args.length; i++) {
      String key = toString(args[i]);
      if (MAP.contains(key)) {
        MAP.remove(key);
        count++;
      }
    }

    returnInteger(count);
    logger.debug("EXISTS -> {}", count);
  }

  private void lpush(Object[] args) throws IOException {
    String key = toString(args[1]);
    RList list = new RList();

    for (int i = 2; i < args.length; i++) {
      list.leftPush(toString(args[i]));
    }

    MAP.put(key, list);
    returnSimple("OK");
  }

  private void rpush(Object[] args) throws IOException {
    String key = toString(args[1]);
    RList list = new RList();

    for (int i = 2; i < args.length; i++) {
      list.rigthPush(toString(args[i]));
    }

    MAP.put(key, list);
    returnSimple("OK");
  }

  private void lindex(Object[] args) throws IOException {
    String key = toString(args[1]);

    if (!exists(key)) {
      returnNil();
      logger.debug("LINDEX {} -> (nil)", key);
      return;
    }

    try {
      RList list = MAP.getList(key);
      Long index = toLong(args[2]);

      String value = list.getValue(index);
      returnBulk(value);
      logger.error("LINDEX {} -> {}", key, value);
    } catch (Exception e) {
      returnError(e.getMessage());
      logger.error("LINDEX {} -> {}", key, e);
    }
  }

  private void lrange(Object[] args) throws IOException {
    String key = toString(args[1]);

    if (!exists(key)) {
      returnNil();
      logger.debug("LINDEX {} -> (nil)", key);
      return;
    }

    try {
      RList list = MAP.getList(key);

      Long sIndex = toLong(args[2]);
      Long eIndex = toLong(args[3]);

      String[] values = list.getValues(sIndex, eIndex);
      returnArray(values);
    } catch (Exception e) {
      returnError(e.getMessage());
      logger.error("LRANGE {} -> {}", key, e);
    }
  }

  private void save() throws IOException {
    FileOutputStream fileStream = new FileOutputStream("dump");
    ObjectOutputStream out = new ObjectOutputStream(fileStream);

    out.writeObject(MAP);
    out.close();
    returnSimple("OK");
  }

  private boolean exists(String key){
    return MAP.contains(key);
  }

  private String toString(Object obj) {
    return (String) obj;
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

  private void returnNil() throws IOException {
    final byte[] NIL = "$-1\r\n".getBytes(UTF_8);
    socket.getOutputStream().write(NIL);
    logger.trace("NIL");
  }
}
