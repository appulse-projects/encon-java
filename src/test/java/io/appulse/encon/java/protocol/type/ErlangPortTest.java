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

import static org.assertj.core.api.Assertions.assertThat;
import static io.appulse.encon.java.protocol.TermType.PORT;
import static io.appulse.encon.java.protocol.TermType.NEW_PORT;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import erlang.OtpErlangPort;
import erlang.OtpOutputStream;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;
import lombok.SneakyThrows;

public class ErlangPortTest {

  @Test
  public void newInstance () {
    val node = "popa";
    val id = 500;
    val creation = 42;

    val bytes = Bytes.allocate()
        .put1B(PORT.getCode())
        .put(new ErlangAtom(node).toBytes())
        .put4B(id & 0xFFFFFFF)
        .put1B(creation & 0x3)
        .array();

    ErlangPort port = ErlangTerm.newInstance(bytes);
    assertThat(port).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(port.getNode())
          .isEqualTo(node);

      softly.assertThat(port.getId())
          .isEqualTo(id & 0xFFFFFFF);

      softly.assertThat(port.getCreation())
          .isEqualTo(creation & 0x3);
    });
  }

  @Test
  public void toBytes () {
    val node = "popa";
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
  public void encode () {
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
