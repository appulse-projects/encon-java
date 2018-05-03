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

import static io.appulse.encon.terms.TermType.BIT_BINNARY;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.exception.ErlangTermValidationException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpErlangBitstr;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangBitStringTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void erlangTermValidationException () {
    assertThatThrownBy(() -> new ErlangBitString(new byte[] { 1 }, -1))
        .isInstanceOf(ErlangTermValidationException.class)
        .hasMessage("Padding must be in range 0..7");

    assertThatThrownBy(() -> new ErlangBitString(new byte[0], 1))
        .isInstanceOf(ErlangTermValidationException.class)
        .hasMessage("Padding on zero length BitString");
  }

  @Test
  public void newInstance () {
    val value = new byte[] { 1, 2, 3 };
    val pad = 3;

    val bytes = Bytes.allocate()
        .put1B(BIT_BINNARY.getCode())
        .put4B(value.length)
        .put1B(8 - pad)
        .put(value)
        .array();

    ErlangBitString bitString = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(bitString).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(bitString.getBits())
          .isEqualTo(new byte[] { 1, 2, 0 });

      softly.assertThat(bitString.getPad())
          .isEqualTo(3);
    });
  }

  @Test
  public void toBytes () {
    val bits = new byte[] { 1, 2, 3 };
    val pad = 3;

    val bytes = Bytes.allocate()
        .put1B(BIT_BINNARY.getCode())
        .put4B(bits.length)
        .put1B(8 - pad)
        .put(new byte[] { 1, 2, 0 })
        .array();

    assertThat(Erlang.bitstr(bits, pad).toBytes())
        .isEqualTo(bytes);
  }

  @Test
  public void encode () {
    val binary = new byte[] { 1, 2, 3 };

    assertThat(Erlang.bitstr(binary, 1).toBytes())
        .isEqualTo(bytes(binary, 1));

    assertThat(Erlang.bitstr(binary, 4).toBytes())
        .isEqualTo(bytes(binary, 4));

    assertThat(Erlang.bitstr(binary, 0).toBytes())
        .isEqualTo(bytes(binary, 0));
  }

  @Test
  public void decode () throws Exception {
    val bits = new byte[] { 1, 2, 3 };
    val pad = 3;

    val bytes = Bytes.allocate()
        .put1B(BIT_BINNARY.getCode())
        .put4B(bits.length)
        .put1B(8 - pad)
        .put(new byte[] { 1, 2, 0 })
        .array();

    try (val input = new OtpInputStream(bytes)) {
      ErlangBitString bitString = ErlangTerm.newInstance(wrappedBuffer(bytes));
      assertThat(bitString.getBits())
          .isEqualTo(input.read_bitstr(new int[1]));
    }
  }

  @SneakyThrows
  private byte[] bytes (byte[] binary, int padBits) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      OtpErlangBitstr bitstr = new OtpErlangBitstr(binary, padBits);
      bitstr.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
