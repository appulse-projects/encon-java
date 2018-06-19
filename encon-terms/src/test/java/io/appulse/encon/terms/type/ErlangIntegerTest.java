/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon.terms.type;

import static io.appulse.encon.terms.TermType.INTEGER;
import static io.appulse.encon.terms.TermType.LARGE_BIG;
import static io.appulse.encon.terms.TermType.SMALL_BIG;
import static io.appulse.encon.terms.TermType.SMALL_INTEGER;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;


import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpErlangLong;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangIntegerTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

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

  @Test
  public void decode () throws Exception {
    val bytes1 = Bytes.allocate()
        .put1B(SMALL_INTEGER.getCode())
        .put1B(255)
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangInteger inte = ErlangTerm.newInstance(wrappedBuffer(bytes1));
      assertThat(inte.asInt())
          .isEqualTo(input.read_int());
    }

    val bytes2 = Bytes.allocate()
        .put1B(INTEGER.getCode())
        .put4B(134217726)
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangInteger inte = ErlangTerm.newInstance(wrappedBuffer(bytes2));
      assertThat(inte.asInt())
          .isEqualTo(input.read_int());
    }

    val bytes3 = bigBytes(BigInteger.valueOf(Long.MAX_VALUE));

    try (val input = new OtpInputStream(bytes3)) {
      ErlangInteger inte = ErlangTerm.newInstance(wrappedBuffer(bytes3));
      assertThat(inte.asLong())
          .isEqualTo(input.read_long());
    }

    int count = 256;
    byte[] bytes = new byte[count];
    IntStream.range(0, count).forEach(it -> bytes[it] = 125);
    BigInteger value = new BigInteger(bytes);
    val bytes4 = bigBytes(value);

    try (val input = new OtpInputStream(bytes4)) {
      ErlangInteger inte = ErlangTerm.newInstance(wrappedBuffer(bytes4));
      OtpErlangLong ll = (OtpErlangLong) input.read_any();

      assertThat(inte.asBigInteger())
          .isEqualTo(ll.bigIntegerValue());

      assertThat(value.equals(ll.bigIntegerValue())).isTrue();
    }
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

  private byte[] bigBytes (BigInteger value) {
    Bytes buffer = Bytes.allocate();

    if (value.abs().toByteArray().length < 256) {
        buffer.put1B(SMALL_BIG.getCode());
    } else {
        buffer.put1B(LARGE_BIG.getCode());
    }

    byte[] bytes = value.abs().toByteArray();
    int index = 0;
    for (; index < bytes.length && bytes[index] == 0; index++) {
    // skip leading zeros
    }

    byte[] magnitude = Arrays.copyOfRange(bytes, index, bytes.length);
    int length = magnitude.length;
    // Reverse the array to make it little endian.
    for (int i = 0, j = length; i < j--; i++) {
    // Swap [i] with [j]
    byte temp = magnitude[i];
    magnitude[i] = magnitude[j];
    magnitude[j] = temp;
    }

    if ((length & 0xFF) == length) {
    buffer.put1B(length); // length
    } else {
    buffer.put4B(length); // length
    }
    val sign = value.signum() < 0
                ? 1
                : 0;
    buffer.put1B(sign);
    buffer.put(magnitude);
    return buffer.array();
  }
}
