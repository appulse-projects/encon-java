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

import static io.appulse.encon.java.protocol.TermType.NEWER_REFERENCE;
import static io.appulse.encon.java.protocol.TermType.NEW_REFERENCE;
import static io.appulse.encon.java.protocol.TermType.REFERENCE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.util.TestMethodNamePrinter;
import io.appulse.utils.Bytes;

import erlang.OtpErlangRef;
import erlang.OtpInputStream;
import erlang.OtpOutputStream;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class ErlangReferenceTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void instantiate () {
    assertThat(ErlangReference.builder()
            .node("popa")
            .id(3)
            .creation(3)
            .build()
            .getType()
        )
        .isEqualTo(NEW_REFERENCE);
  }

  @Test
  public void newInstance () {
    val node = "popa@localhost";
    val ids = new int[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(ids.length)
        .put(new ErlangAtom(node).toBytes())
        .put1B(creation)
        .put4B(ids[0]);

    IntStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::put4B);

    val expected = builder.array();

    ErlangReference reference = ErlangTerm.newInstance(expected);
    assertThat(reference).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(node);

      softly.assertThat(reference.getIds())
          .isEqualTo(ids);

      softly.assertThat(reference.getId())
          .isEqualTo(ids[0]);

      softly.assertThat(reference.getCreation())
          .isEqualTo(creation & 0x3);
    });
  }

  @Test
  public void toBytes () {
    val node = "popa@localhost";
    val ids = new int[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(ids.length)
        .put(new ErlangAtom(node).toBytes())
        .put1B(creation & 0x3)
        .put4B(ids[0] & 0x3FFFF);

    IntStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::put4B);

    val expected = builder.array();

    assertThat(ErlangReference.builder()
            .node(node)
            .ids(ids)
            .creation(creation)
            .build()
            .toBytes()
        )
        .isEqualTo(expected);
  }

  @Test
  public void encode () {
    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .ids(new int[] { 1 })
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(NEW_REFERENCE.getCode(), "popa@localhost", new int[] { 1 }, 4));

    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .type(NEWER_REFERENCE)
        .ids(new int[] { 1 })
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(NEWER_REFERENCE.getCode(), "popa@localhost", new int[] { 1 }, 4));

    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .id(42)
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(NEW_REFERENCE.getCode(), "popa@localhost", new int[] { 42 }, 4));
  }

  @Test
  public void decode () throws Exception {
    byte[] bytes1 = Bytes.allocate()
        .put1B(REFERENCE.getCode())
        .put(new ErlangAtom("popa@localhost").toBytes())
        .put4B(Integer.MAX_VALUE)
        .put1B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangReference reference = ErlangTerm.newInstance(bytes1);
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(otpRef.ids());

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }

    byte[] bytes2 = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(3)
        .put(new ErlangAtom("popa@localhost").toBytes())
        .put1B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangReference reference = ErlangTerm.newInstance(bytes2);
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(otpRef.ids());

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }

    byte[] bytes3 = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(3)
        .put(new ErlangAtom("popa@localhost").toBytes())
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .put4B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes3)) {
      ErlangReference reference = ErlangTerm.newInstance(bytes3);
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(otpRef.ids());

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }
  }

  @SneakyThrows
  private byte[] bytes (int tag, String node, int[] ids, int creation) {
    OtpErlangRef reference = new OtpErlangRef(tag, node, ids, creation);
    try (OtpOutputStream output = new OtpOutputStream()) {
      reference.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }
}
