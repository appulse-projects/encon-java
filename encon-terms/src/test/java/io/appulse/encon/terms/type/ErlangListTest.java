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

import static io.appulse.encon.terms.TermType.LIST;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;

import erlang.OtpErlangAtom;
import erlang.OtpErlangList;
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
@DisplayName("Check Erlang's List term type")
class ErlangListTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("create new instance from bytes")
  void newInstance () {
    val value = new ErlangNil();
    val bytes = Bytes.allocate()
        .put1B(LIST.getCode())
        .put4B(1)
        .put(value.toBytes())
        .put(new ErlangNil().toBytes())
        .array();

    ErlangList list = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(list).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(list.isCollectionTerm())
          .isTrue();

      softly.assertThat(list.isList())
          .isTrue();

      softly.assertThat(list.get(0))
          .isPresent()
          .hasValue(value);

      softly.assertThat(list.size())
          .isEqualTo(1);

      softly.assertThat(list.getTail())
          .isEqualTo(new ErlangNil());
    });
  }

  @Test
  @DisplayName("convert instance to byte array")
  void toBytes () {
    val value = new ErlangNil();
    val expected = Bytes.allocate()
        .put1B(LIST.getCode())
        .put4B(1)
        .put(value.toBytes())
        .put(new ErlangNil().toBytes())
        .array();

    assertThat(new ErlangList(value).toBytes())
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("encode instance to byte array and compare with jinterface output")
  void encode () {
    String[] values = new String[] {
        "one",
        "two",
        "three"
    };

    ErlangAtom[] atoms = Stream.of(values)
        .map(ErlangAtom::new)
        .toArray(ErlangAtom[]::new);

    assertThat(new ErlangList(atoms).toBytes())
        .isEqualTo(bytes(values));
  }

  @SneakyThrows
  private byte[] bytes (String[] values) {
    OtpErlangAtom[] atoms = Stream.of(values)
        .map(OtpErlangAtom::new)
        .toArray(OtpErlangAtom[]::new);

    OtpErlangList list = new OtpErlangList(atoms);
    try (OtpOutputStream output = new OtpOutputStream()) {
      list.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
