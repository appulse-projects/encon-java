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

import static io.appulse.encon.terms.TermType.BINARY;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;


import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpErlangBinary;
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
public class ErlangBinaryTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void instantiate () {
    val value = new byte[] { 1, 2, 3 };

    assertThat(new ErlangBinary(value).asBinary())
        .isEqualTo(value);
  }

  @Test
  public void newInstance () {
    val value = new byte[] { 1, 2, 3 };

    val bytes = Bytes.allocate()
        .put1B(BINARY.getCode())
        .put4B(value.length)
        .put(value)
        .array();

    ErlangBinary binary = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(binary).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(binary.isBinary())
          .isTrue();

      softly.assertThat(binary.asBinary())
          .isEqualTo(value);
    });
  }

  @Test
  public void toBytes () {
    val value = new byte[] { 1, 2, 3 };

    val expected = Bytes.allocate()
        .put1B(BINARY.getCode())
        .put4B(value.length)
        .put(value)
        .array();

    assertThat(Erlang.binary(value).toBytes())
        .isEqualTo(expected);
  }

  @Test
  public void encode () {
    val binary = new byte[] { 1, 2, 3, 4, 5 };
    assertThat(Erlang.binary(binary).toBytes())
        .isEqualTo(bytes(binary));
  }

  @Test
  public void decode () throws Exception {
    val value = new byte[] { 1, 2, 3 };

    val bytes = Bytes.allocate()
        .put1B(BINARY.getCode())
        .put4B(value.length)
        .put(value)
        .array();

    try (val input = new OtpInputStream(bytes)) {
      ErlangBinary binary = ErlangTerm.newInstance(wrappedBuffer(bytes));
      assertThat(binary.asBinary())
          .isEqualTo(input.read_binary());
    }
  }

  @SneakyThrows
  private byte[] bytes (byte[] value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      OtpErlangBinary binary = new OtpErlangBinary(value);
      binary.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
