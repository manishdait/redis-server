package io.github.manishdait.redis_server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RespDeserializer {
  private InputStream in;

  RespDeserializer(String str) {
    this(new ByteArrayInputStream(str.getBytes()));
  }

  RespDeserializer(InputStream stream) {
    this.in = stream;
  }

  public Object decode() throws IOException {
    int prefix = in.read();

    if (prefix == -1) return null;

    switch (prefix) {
      case '+': return readSimpleString();

      case ':': return readInteger();

      case '$': return readBulkString();

      case '*': return readArray();

      default: throw new RuntimeException("Invalid RESP prefix: " + prefix);
    }
  }

  private String readSimpleString() throws IOException {
    String line = readLine();
    if (line.contains("\r") || line.contains("\n")) {
      throw new IOException("Invalid Simple String: contains CR or LF");
    } 

    return line;
  }

  private Long readInteger() throws IOException {
    return Long.parseLong(readLine());
  }

  private String readBulkString() throws IOException {
    int length = Integer.parseInt(readLine());

    if(length == -1) return null;

    byte[] buf = new byte[length];
    in.read(buf);

    int cr = in.read();
    int lf = in.read();

    if (cr != '\r' || lf != '\n') {
      throw new IOException("Bulk string not properly terminated");
    }

    return new String(buf);
  }

  private Object[] readArray() throws IOException {
    int count = Integer.parseInt(readLine());

    if (count == -1) {
      return null;
    }

    Object[] arr = new Object[count];
    for (int i = 0; i < count; i++) {
      arr[i] = decode();
    }
    
    return arr;
  }

  private String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();

    int ch;
    while ((ch = in.read()) != -1) {
      if(ch == '\r') {
        int next = in.read();

        if (next == '\n') break;
        sb.append((char) ch).append((char) next);
      } else {
        sb.append((char) ch);
      }
    }

    return sb.toString();
  }
}
