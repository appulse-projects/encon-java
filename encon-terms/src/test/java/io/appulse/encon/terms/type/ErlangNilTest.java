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

import static io.appulse.encon.terms.TermType.NIL;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;

import erlang.OtpOutputStream;
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
@DisplayName("Check Erlang's NIL term type")
class ErlangNilTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("create new instance from bytes")
  void newInstance () {
    val bytes = Bytes.resizableArray()
        .write1B(NIL.getCode())
        .array();

    ErlangNil nil = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(nil).isNotNull();
    assertThat(nil.getType())
        .isEqualTo(NIL);
  }

  @Test
  @DisplayName("convert instance to byte array")
  void toBytes () {
    val expected = Bytes.resizableArray()
        .write1B(NIL.getCode())
        .array();

    assertThat(new ErlangNil().toBytes())
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("encode instance to byte array and compare with jinterface output")
  void encode () {
    assertThat(new ErlangNil().toBytes())
        .isEqualTo(bytes());
  }

  @SneakyThrows
  private byte[] bytes () {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_nil();
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
