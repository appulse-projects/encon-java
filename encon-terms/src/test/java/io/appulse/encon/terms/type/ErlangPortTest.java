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

import static io.appulse.encon.terms.TermType.NEW_PORT;
import static io.appulse.encon.terms.TermType.PORT;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;

import erlang.OtpErlangPort;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@DisplayName("Check Erlang's Port term type")
class ErlangPortTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("create new instance from bytes")
  void newInstance () {
    val node = "popa@localhost";
    val id = 500;
    val creation = 42;

    val bytes = Bytes.allocate()
        .put1B(PORT.getCode())
        .put(new ErlangAtom(node).toBytes())
        .put4B(id & 0xFFFFFFF)
        .put1B(creation & 0x3)
        .array();

    ErlangPort port = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(port).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(port.getDescriptor().getFullName())
          .isEqualTo(node);

      softly.assertThat(port.getId())
          .isEqualTo(id & 0xFFFFFFF);

      softly.assertThat(port.getCreation())
          .isEqualTo(creation & 0x3);
    });
  }

  @Test
  @DisplayName("convert instance to byte array")
  void toBytes () {
    val node = "popa@localhost";
    val id = 500;
    val creation = 42;

    val expected = Bytes.allocate()
        .put1B(PORT.getCode())
        .put(new ErlangAtom(node).toBytes())
        .put4B(id & 0xFFFFFFF)
        .put1B(creation & 0x3)
        .array();

    assertThat(ErlangPort.builder()
            .node(node)
            .id(id)
            .creation(creation)
            .build()
            .toBytes()
        )
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("encode instance to byte array and compare with jinterface output")
  void encode () {
    assertThat(ErlangPort.builder()
            .node("popa@localhost")
            .id(1)
            .creation(3)
            .build()
            .toBytes()
    )
    .isEqualTo(bytes(PORT.getCode(), "popa@localhost", 1, 3));

    assertThat(ErlangPort.builder()
            .node("popa@localhost")
            .type(NEW_PORT)
            .id(1)
            .creation(3)
            .build()
            .toBytes()
    )
    .isEqualTo(bytes(NEW_PORT.getCode(), "popa@localhost", 1, 3));
  }

  @Test
  @DisplayName("decode instance from byte array and compare with jinterface result")
  void decode () throws Exception {
    byte[] bytes1 = Bytes.allocate()
        .put1B(PORT.getCode())
        .put(new ErlangAtom("popa@localhost").toBytes())
        .put4B(Integer.MAX_VALUE)
        .put1B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangPort port = ErlangTerm.newInstance(wrappedBuffer(bytes1));
      OtpErlangPort otpPid = input.read_port();

      assertThat(port.getDescriptor().getFullName())
          .isEqualTo(otpPid.node());

      assertThat(port.getId())
          .isEqualTo(otpPid.id());

      assertThat(port.getCreation())
          .isEqualTo(otpPid.creation());
    }

    byte[] bytes2 = Bytes.allocate()
        .put1B(NEW_PORT.getCode())
        .put(new ErlangAtom("popa@localhost").toBytes())
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangPort port = ErlangTerm.newInstance(wrappedBuffer(bytes2));
      OtpErlangPort otpPid = input.read_port();

      assertThat(port.getDescriptor().getFullName())
          .isEqualTo(otpPid.node());

      assertThat(port.getId())
          .isEqualTo(otpPid.id());

      assertThat(port.getCreation())
          .isEqualTo(otpPid.creation());
    }
  }

  @SneakyThrows
  private byte[] bytes (int tag, String node, int id, int creation) {
    OtpErlangPort port = new OtpErlangPort(tag, node, id, creation);
    try (OtpOutputStream output = new OtpOutputStream()) {
      port.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
