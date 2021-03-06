/*
 * Copyright 2020 the original author or authors.
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

import static io.appulse.encon.terms.TermType.NEW_PID;
import static io.appulse.encon.terms.TermType.PID;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import erlang.OtpErlangPid;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ErlangPidTest {

  @Test
  public void newInstance () {
    val node = "popa";
    val id = 500;
    val serial = 10;
    val creation = 42;

    val bytes = Bytes.resizableArray()
        .write1B(PID.getCode())
        .writeNB(new ErlangAtom(node).toBytes())
        .write4B(id & 0x7FFF)
        .write4B(serial & 0x1FFF)
        .write1B(creation & 0x3)
        .arrayCopy();

    ErlangPid pid = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(pid).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(pid.getDescriptor().getNodeName())
          .isEqualTo(node);

      softly.assertThat(pid.getId())
          .isEqualTo(id & 0x7FFF);

      softly.assertThat(pid.getSerial())
          .isEqualTo(serial & 0x1FFF);

      softly.assertThat(pid.getCreation())
          .isEqualTo(creation & 0x3);
    });
  }

  @Test
  public void toBytes () {
    val node = "popa";
    val id = 500;
    val serial = 10;
    val creation = 42;

    val expected = Bytes.resizableArray()
        .write1B(PID.getCode())
        .writeNB(new ErlangAtom(node).toBytes())
        .write4B(id & 0x7FFF)
        .write4B(serial & 0x1FFF)
        .write1B(creation & 0x3)
        .arrayCopy();

    assertThat(ErlangPid.builder()
        .node(node)
        .id(id)
        .serial(serial)
        .creation(creation)
        .build()
        .toBytes()
    )
        .isEqualTo(expected);
  }

  @Test
  public void encode () {
    assertThat(ErlangPid.builder()
        .node("popa@localhost")
        .id(1)
        .serial(27)
        .creation(3)
        .build()
        .toBytes()
    )
        .isEqualTo(bytes(PID.getCode(), "popa@localhost", 1, 27, 3));

    assertThat(ErlangPid.builder()
        .node("popa@localhost")
        .type(NEW_PID)
        .id(1)
        .serial(27)
        .creation(3)
        .build()
        .toBytes()
    )
        .isEqualTo(bytes(NEW_PID.getCode(), "popa@localhost", 1, 27, 3));
  }

  @Test
  public void decode () throws Exception {
    byte[] bytes1 = Bytes.resizableArray()
        .write1B(PID.getCode())
        .writeNB(new ErlangAtom("popa@localhost").toBytes())
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write1B(Integer.MAX_VALUE)
        .arrayCopy();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangPid pid = ErlangTerm.newInstance(wrappedBuffer(bytes1));
      OtpErlangPid otpPid = input.read_pid();

      assertThat(pid.getDescriptor().getFullName())
          .isEqualTo(otpPid.node());

      assertThat(pid.getId())
          .isEqualTo(otpPid.id());

      assertThat(pid.getCreation())
          .isEqualTo(otpPid.creation());

      assertThat(pid.getSerial())
          .isEqualTo(otpPid.serial());
    }

    byte[] bytes2 = Bytes.resizableArray()
        .write1B(NEW_PID.getCode())
        .writeNB(new ErlangAtom("popa@localhost").toBytes())
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .arrayCopy();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangPid pid = ErlangTerm.newInstance(wrappedBuffer(bytes2));
      OtpErlangPid otpPid = input.read_pid();

      assertThat(pid.getDescriptor().getFullName())
          .isEqualTo(otpPid.node());

      assertThat(pid.getId())
          .isEqualTo(otpPid.id());

      assertThat(pid.getCreation())
          .isEqualTo(otpPid.creation());

      assertThat(pid.getSerial())
          .isEqualTo(otpPid.serial());
    }
  }

  @Test
  public void equality () {
    val pid1 = ErlangPid.builder()
        .type(PID)
        .node("node1")
        .id(28)
        .serial(97)
        .creation(3)
        .build();

    pid1.getDescriptor();

    val pid2 = ErlangPid.builder()
        .type(NEW_PID)
        .node("node1")
        .id(28)
        .serial(97)
        .creation(3)
        .build();

    val pid3 = ErlangPid.builder()
        .type(NEW_PID)
        .node("node1")
        .id(28)
        .serial(96)
        .creation(3)
        .build();

    val pid4 = ErlangPid.builder()
        .type(NEW_PID)
        .node("node2")
        .id(28)
        .serial(96)
        .creation(3)
        .build();

    assertThat(pid1).isEqualTo(pid2);
    assertThat(pid2).isNotEqualTo(pid3);
    assertThat(pid3).isNotEqualTo(pid4);
  }

  @SneakyThrows
  private byte[] bytes (int tag, String node, int id, int serial, int creation) {
    OtpErlangPid pid = new OtpErlangPid(tag, node, id, serial, creation);
    try (OtpOutputStream output = new OtpOutputStream()) {
      pid.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
