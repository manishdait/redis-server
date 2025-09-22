package io.github.manishdait;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

public class RespDeserializerTest {
  @Test
  void shouldDecodeSimpleString() throws IOException {
    final Object result = new RespDeserializer("+OK\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .isEqualTo("OK");
  }

  @Test
  void shouldDecodeSimpleStringWithSpace() throws IOException {
    final Object result = new RespDeserializer("+Hello World\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .isEqualTo("Hello World");
  }

  @Test
  void shouldDecodeSimpleStringWithIntegerAndSymbol() throws IOException {
    final Object result = new RespDeserializer("+Hello1 23!@#\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .isEqualTo("Hello1 23!@#");
  }

  @Test
  void shouldDecodePositiveInteger() throws IOException {
    final Object result = new RespDeserializer(":42\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(Long.class)
      .isEqualTo(42L);
  }

  @Test
  void shouldDecodeNegativeInteger() throws IOException {
    final Object result = new RespDeserializer(":-42\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(Long.class)
      .isEqualTo(-42L);
  }

  @Test
  void shouldDecodeIntegerValueAsZero() throws IOException {
    final Object result = new RespDeserializer(":0\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(Long.class)
      .isEqualTo(0L);
  }

  @Test
  void shouldDecodeRegularBulkStrings() throws IOException {
    final Object result = new RespDeserializer("$5\r\nhello\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .isEqualTo("hello");
  }

  @Test
  void shouldDecodeEmptyBulkStrings() throws IOException {
    final Object result = new RespDeserializer("$0\r\n\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .asInstanceOf(InstanceOfAssertFactories.STRING).isEmpty();
  }

  @Test
  void shouldDecodeBulkStringsWithNewLines() throws IOException {
    final Object result = new RespDeserializer("$11\r\nHello\nWorld\r\n").decode();
    Assertions.assertThat(result).isNotNull()
      .isInstanceOf(String.class)
      .isEqualTo("Hello\nWorld");
  }

  @Test
  void shouldDecodeNullBulkStringsr() throws IOException {
    final Object result = new RespDeserializer("$-1\r\n").decode();
    Assertions.assertThat(result).isNull();
  }

  @Test
  void shouldDecodeEmptyArray() throws IOException {
    final Object result = new RespDeserializer("*0\r\n").decode();
    Assertions.assertThat(result)
      .isInstanceOf(Object[].class)
      .asInstanceOf(InstanceOfAssertFactories.ARRAY).isEmpty();
  }

  @Test
  void shouldDecodeNullArray() throws IOException {
    final Object result = new RespDeserializer("*-1\r\n").decode();
    Assertions.assertThat(result).isNull();
  }

  @Test
  void shouldDecodeArrayOfBulkStrings() throws IOException {
    final Object result = new RespDeserializer("*3\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$5\r\nhello\r\n").decode();
    Assertions.assertThat(result).isInstanceOf(Object[].class);
    
    final Object[] arr = (Object[]) result;
    Assertions.assertThat(arr).containsExactly("foo", "bar", "hello");
  }

  @Test
  void shouldDecodeArrayOfBulkStringsAndNull() throws IOException {
    final Object result = new RespDeserializer("*3\r\n$3\r\nfoo\r\n$3\r\nbar\r\n$-1\r\n").decode();
    Assertions.assertThat(result).isInstanceOf(Object[].class);
    
    final Object[] arr = (Object[]) result;
    Assertions.assertThat(arr).containsExactly("foo", "bar", null);
  }

  @Test
  void shouldDecodeNestedArray() throws IOException {
    final String input = "*2\r\n*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n*1\r\n$5\r\nhello\r\n";
    final Object result = new RespDeserializer(input).decode();
    Assertions.assertThat(result).isInstanceOf(Object[].class);
    
    final Object[] outer = (Object[]) result;
    Assertions.assertThat(outer[0]).isInstanceOf(Object[].class);
    
    final Object[] inner1 = (Object[]) outer[0];
    Assertions.assertThat(inner1).containsExactly("foo", "bar");
    
    final Object[] inner2 = (Object[]) outer[1];
    Assertions.assertThat(inner2).containsExactly("hello");
  }
}
