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

package io.appulse.encon.java.protocol.type;

import static io.appulse.encon.java.protocol.TermType.PID;
import static io.appulse.encon.java.protocol.TermType.NEW_PID;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import erlang.OtpErlangPid;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;

public class ErlangPidTest {

  @Test
  public void newInstance () {
    val node = "popa";
    val id = 500;
    val serial = 10;
    val creation = 42;

    val bytes = Bytes.allocate()
        .put1B(PID.getCode())
        .put(new ErlangAtom(node).toBytes())
        .put4B(id & 0x7FFF)
        .put4B(serial & 0x1FFF)
        .put1B(creation & 0x3)
        .array();

    ErlangPid pid = ErlangTerm.newInstance(bytes);
    assertThat(pid).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(pid.getDescriptor().getShortName())
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

    val expected = Bytes.allocate()
        .put1B(PID.getCode())
        .put(new ErlangAtom(NodeDescriptor.from(node).getFullName()).toBytes())
        .put4B(id & 0x7FFF)
        .put4B(serial & 0x1FFF)
        .put1B(creation & 0x3)
        .array();

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
