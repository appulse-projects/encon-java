
package io.appulse.encon.java.protocol.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.Test;
import erlang.OtpErlangString;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;

public class ErlangStringTest {

  @Test
  public void encode () {
    assertThat(new ErlangString("").toBytes())
        .isEqualTo(bytes(""));

    assertThat(new ErlangString("popa").toBytes())
        .isEqualTo(bytes("popa"));

    val string1 = repeat("a", 65536);
    assertThat(new ErlangString(string1).toBytes())
        .isEqualTo(bytes(string1));

    val string2 = repeat("я", 65536);
    assertThat(new ErlangString(string2).toBytes())
        .isEqualTo(bytes(string2));
  }

  private String repeat (String string, int times) {
    StringBuilder sb = new StringBuilder(string.length() * times);
    IntStream.range(0, times).forEach(it -> sb.append(string));
    return sb.toString();
  }

  @SneakyThrows
  private byte[] bytes (String value) {
    OtpErlangString string = new OtpErlangString(value);
    try (OtpOutputStream output = new OtpOutputStream()) {
      string.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
