/*
 * Copyright 2019 the original author or authors.
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

import static io.appulse.encon.terms.TermType.FLOAT;
import static io.appulse.encon.terms.TermType.NEW_FLOAT;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import erlang.OtpErlangDouble;
import erlang.OtpErlangFloat;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@DisplayName("Check Erlang's Float term type")
class ErlangFloatTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("encode instance to byte array and compare with jinterface output")
  void encode () {
    assertThat(Erlang.number(Float.MIN_NORMAL).toBytes())
        .isEqualTo(bytes(Float.MIN_NORMAL));

    assertThat(Erlang.number(Float.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Float.MAX_VALUE));

    assertThat(Erlang.number(Float.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Float.MIN_VALUE));

    assertThat(Erlang.number(Double.MIN_NORMAL).toBytes())
        .isEqualTo(bytes(Double.MIN_NORMAL));

    assertThat(Erlang.number(Double.MAX_VALUE).toBytes())
        .isEqualTo(bytes(Double.MAX_VALUE));

    assertThat(Erlang.number(Double.MIN_VALUE).toBytes())
        .isEqualTo(bytes(Double.MIN_VALUE));
  }

  @Test
  @DisplayName("decode instance from byte array and compare with jinterface result")
  void decode () throws Exception {
    val bytes1 = Bytes.allocate()
        .put1B(FLOAT.getCode())
        .put(String.format("%031.20e", Float.MAX_VALUE).getBytes(ISO_8859_1))
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangFloat fl = ErlangTerm.newInstance(wrappedBuffer(bytes1));
      assertThat(fl.asFloat())
          .isEqualTo(input.read_float());
    }

    val bytes2 = Bytes.allocate()
        .put1B(NEW_FLOAT.getCode())
        .put8B(Double.doubleToLongBits(Double.MIN_VALUE))
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangFloat fl = ErlangTerm.newInstance(wrappedBuffer(bytes2));
      assertThat(fl.asDouble())
          .isEqualTo(input.read_double());
    }
  }

  @SneakyThrows
  private byte[] bytes (float value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      OtpErlangFloat floa = new OtpErlangFloat(value);
      floa.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  @SneakyThrows
  private byte[] bytes (double value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      OtpErlangDouble doub = new OtpErlangDouble(value);
      doub.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
