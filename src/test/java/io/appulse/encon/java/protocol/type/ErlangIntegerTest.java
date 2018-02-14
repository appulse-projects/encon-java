
package io.appulse.encon.java.protocol.type;

import static org.assertj.core.api.Assertions.assertThat;
import static io.appulse.encon.java.protocol.TermType.INTEGER;
import static io.appulse.encon.java.protocol.TermType.LARGE_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_INTEGER;

import java.math.BigInteger;
import java.util.stream.IntStream;

import org.junit.Test;

import erlang.OtpErlangLong;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;

public class ErlangIntegerTest {

  @Test
  public void instantiate () {
    assertThat(new ErlangInteger(254).getType())
        .isEqualTo(SMALL_INTEGER);

    assertThat(new ErlangInteger(134217726).getType())
        .isEqualTo(INTEGER);

    assertThat(new ErlangInteger(Long.MAX_VALUE).getType())
        .isEqualTo(SMALL_BIG);

    assertThat(new ErlangInteger(new BigInteger("9223372036854775807000000")).getType())
        .isEqualTo(SMALL_BIG);

    int count = 256;
    byte[] bytes = new byte[count];
    IntStream.range(0, count).forEach(it -> bytes[it] = 125);
    assertThat(new ErlangInteger(new BigInteger(bytes)).getType())
        .isEqualTo(LARGE_BIG);
  }

  @Test
  public void encode () {
    assertThat(new ErlangInteger(Character.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Character.MIN_VALUE));

    assertThat(new ErlangInteger(Character.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Character.MAX_VALUE));

    assertThat(new ErlangInteger(Byte.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Byte.MIN_VALUE));

    assertThat(new ErlangInteger(Byte.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Byte.MAX_VALUE));

    assertThat(new ErlangInteger(Short.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Short.MIN_VALUE));

    assertThat(new ErlangInteger(Short.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Short.MAX_VALUE));

    assertThat(new ErlangInteger(Integer.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Integer.MIN_VALUE));

    assertThat(new ErlangInteger(Integer.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Integer.MAX_VALUE));

    assertThat(new ErlangInteger(Long.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Long.MIN_VALUE));

    assertThat(new ErlangInteger(Long.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Long.MAX_VALUE));

    assertThat(new ErlangInteger(BigInteger.TEN).toBytes())
        .isEqualTo(bytes(BigInteger.TEN));

    assertThat(new ErlangInteger(new BigInteger("9223372036854775807000000")).toBytes())
        .isEqualTo(bytes(new BigInteger("9223372036854775807000000")));

    int count = 256;
    byte[] bytes = new byte[count];
    IntStream.range(0, count).forEach(it -> bytes[it] = 125);
    BigInteger value = new BigInteger(bytes);
    assertThat(new ErlangInteger(value).toBytes())
        .isEqualTo(bytes(value));
  }

  @SneakyThrows
  private byte[] bytes (char value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_char(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (byte value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_byte(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (short value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_short(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (int value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_int(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (long value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_long(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (BigInteger value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      OtpErlangLong lon = new OtpErlangLong(value);
      lon.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
