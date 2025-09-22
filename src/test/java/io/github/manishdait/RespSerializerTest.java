package io.github.manishdait;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class RespSerializerTest {
  @Test
  void shouldEncodeToSimpleString() {
    final String result = RespSerializer.toSimpleString("OK");
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("+OK\r\n");
  }

  @Test
  void shouldEncodeToSimpleStringWithSpace() {
    final String result = RespSerializer.toSimpleString("Hello World");
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("+Hello World\r\n");
  } 

  @Test
  void shouldEncodeToSimpleStringWithSymbolsAndInteger() {
    final String result = RespSerializer.toSimpleString("HelloWorld#123");
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("+HelloWorld#123\r\n");
  }

  @Test
  void shouldEncodeToInteger() {
    final String result = RespSerializer.toInteger(41);
    Assertions.assertThat(result).isNotNull()
      .isEqualTo(":41\r\n");
  }

  @Test
  void shouldEncodeToIntegerForNegativeNumber() {
    final String result = RespSerializer.toInteger(-41);
    Assertions.assertThat(result).isNotNull()
      .isEqualTo(":-41\r\n");
  }

  @Test
  void shouldEncodeToBulkString() {
    final String result = RespSerializer.toBulkString("hello");
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("$5\r\nhello\r\n");
  }

  @Test
  void shouldEncodeToBulkStringWithNewLine() {
    final String str = "Hello\nWorld";
    final String result = RespSerializer.toBulkString(str);
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("$11\r\nHello\nWorld\r\n");
  }

  @Test
  void shouldEncodeToArray() {
    final Object[] arr = {"foo", "bar", "hello"};
    final String result = RespSerializer.toArray(arr);
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("*3\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$5\r\nhello\r\n");
  }

  @Test
  void shouldEncodeToArrayWithNumbers() {
    final Object[] arr = {"foo", "bar", 5};
    final String result = RespSerializer.toArray(arr);
    Assertions.assertThat(result).isNotNull()
      .isEqualTo("*3\r\n$3\r\nfoo\r\n$3\r\nbar\r\n:5\r\n");
  }
}
