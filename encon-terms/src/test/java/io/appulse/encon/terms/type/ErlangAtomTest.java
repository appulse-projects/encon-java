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

import static io.appulse.encon.terms.TermType.ATOM_UTF8;
import static io.appulse.encon.terms.TermType.SMALL_ATOM_UTF8;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;


import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;
import io.appulse.utils.test.TestMethodNamePrinter;

import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import io.netty.buffer.Unpooled;
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
public class ErlangAtomTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void instantiate () {
    assertThat(new ErlangAtom("hello").getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(new ErlangAtom(repeat("попа", 300)).getType())
        .isEqualTo(ATOM_UTF8);

    assertThat(new ErlangAtom(true).getType())
        .isEqualTo(SMALL_ATOM_UTF8);
  }

  @Test
  public void newInstance () {
    val value = "hello";
    val bytes = Bytes.allocate()
        .put1B(SMALL_ATOM_UTF8.getCode())
        .put1B(value.getBytes(UTF_8).length)
        .put(value.getBytes(UTF_8))
        .array();

    ErlangAtom atom = ErlangTerm.newInstance(wrappedBuffer(bytes));
    assertThat(atom).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(atom.isTextual())
          .isTrue();

      softly.assertThat(atom.asText())
          .isEqualTo(value);

      softly.assertThat(atom.asBoolean())
          .isFalse();
    });
  }

  @Test
  public void toBytes () {
    val value = "hello";
    val expected = Bytes.allocate()
        .put1B(SMALL_ATOM_UTF8.getCode())
        .put1B(value.getBytes(UTF_8).length)
        .put(value.getBytes(UTF_8))
        .array();

    assertThat(Erlang.atom(value).toBytes())
        .isEqualTo(expected);
  }

  @Test
  public void encode () {
    val smallValue = "popa";
    val smallAtom = new ErlangAtom(smallValue);

    assertThat(smallAtom.getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(smallAtom.toBytes())
        .isEqualTo(bytes(smallValue));


    val value = repeat(" ", 300);
    val valueAtom = new ErlangAtom(value);

    assertThat(valueAtom.getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(valueAtom.toBytes())
        .isEqualTo(bytes(value));


    val bigValue = repeat("попа", 300);
    val bigAtom = new ErlangAtom(bigValue);

    assertThat(bigAtom.getType())
        .isEqualTo(ATOM_UTF8);

    assertThat(bigAtom.toBytes())
        .isEqualTo(bytes(bigValue));
  }

  @Test
  public void decode () throws Exception {
    val value1 = "hello";
    val bytes1 = Bytes.allocate()
        .put1B(SMALL_ATOM_UTF8.getCode())
        .put1B(value1.getBytes(UTF_8).length)
        .put(value1.getBytes(UTF_8))
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangAtom atom = ErlangTerm.newInstance(Unpooled.wrappedBuffer(bytes1));
      assertThat(atom.asText())
          .isEqualTo(input.read_atom());
    }


    val value2 = "попа";
    val bytes2 = Bytes.allocate()
        .put1B(ATOM_UTF8.getCode())
        .put2B(value2.getBytes(UTF_8).length)
        .put(value2.getBytes(UTF_8))
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangAtom atom = ErlangTerm.newInstance(Unpooled.wrappedBuffer(bytes2));
      assertThat(atom.asText())
          .isEqualTo(input.read_atom());
    }
  }

  private String repeat (String string, int times) {
    StringBuilder sb = new StringBuilder(string.length() * times);
    IntStream.range(0, times).forEach(it -> sb.append(string));
    return sb.toString();
  }

  @SneakyThrows
  private byte[] bytes (String value) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      output.write_atom(value);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
