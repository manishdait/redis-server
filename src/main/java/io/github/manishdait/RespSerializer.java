package io.github.manishdait;

import java.nio.charset.StandardCharsets;

public class RespSerializer {
  private static final String CRLF = "\r\n";

  public static String toSimpleString(String str) {
    return "+" + str + CRLF;
  }

  public static String toBulkString(String str) {
    int length = str.getBytes(StandardCharsets.UTF_8).length;
    return "$" + length + CRLF + str + CRLF;
  }

  public static String toInteger(long num) {
    return ":" + num + CRLF;
  }

  public static String toError(String error) {
    return "-" + error + CRLF;
  }

  public static String toArray(Object[] arr) {
    int count = arr.length;
    StringBuilder sb = new StringBuilder();
    sb.append("*").append(count).append(CRLF);

    for (Object obj : arr) {
      if (obj instanceof String) {
        sb.append(toBulkString((String) obj));
      } else if (obj instanceof Number) {
        sb.append(toInteger(((Number)obj).longValue()));
      } else if (obj instanceof Object[]) {
        sb.append(toArray((Object[]) obj));
      }
    }

    return sb.toString();
  }
}
