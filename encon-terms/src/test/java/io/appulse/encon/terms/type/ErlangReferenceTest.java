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

import static io.appulse.encon.terms.TermType.NEWER_REFERENCE;
import static io.appulse.encon.terms.TermType.NEW_REFERENCE;
import static io.appulse.encon.terms.TermType.REFERENCE;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.LongStream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.utils.Bytes;

import erlang.OtpErlangRef;
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
@DisplayName("Check Erlang's Reference term type")
class ErlangReferenceTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("create new instance by constructor")
  void instantiate () {
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
  @DisplayName("create new instance from bytes")
  void newInstance () {
    val node = "popa@localhost";
    val ids = new long[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.resizableArray()
        .write1B(NEW_REFERENCE.getCode())
        .write2B(ids.length)
        .writeNB(new ErlangAtom(node).toBytes())
        .write1B(creation)
        .write4B(ids[0]);

    LongStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::write4B);

    val expected = builder.array();

    ErlangReference reference = ErlangTerm.newInstance(wrappedBuffer(expected));
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
  @DisplayName("convert instance to byte array")
  void toBytes () {
    val node = "popa@localhost";
    val ids = new long[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.resizableArray()
        .write1B(NEW_REFERENCE.getCode())
        .write2B(ids.length)
        .writeNB(new ErlangAtom(node).toBytes())
        .write1B(creation & 0x3)
        .write4B(ids[0] & 0x3FFFF);

    LongStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::write4B);

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
  @DisplayName("encode instance to byte array and compare with jinterface output")
  void encode () {
    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .ids(new long[] { 1 })
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(new OtpErlangRef(NEW_REFERENCE.getCode(), "popa@localhost", new int[] { 1 }, 4)));

    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .type(NEWER_REFERENCE)
        .ids(new long[] { 1 })
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(new OtpErlangRef(NEWER_REFERENCE.getCode(), "popa@localhost", new int[] { 1 }, 4)));

    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .ids(new long[] { 42 })
        .creation(4)
        .build()
        .toBytes()
    )
    .isEqualTo(bytes(new OtpErlangRef(NEW_REFERENCE.getCode(), "popa@localhost", new int[] { 42 }, 4)));

    assertThat(ErlangReference.builder()
        .node("popa@localhost")
        .id(3)
        .creation(3)
        .build()
        .toBytes()
    ).isEqualTo(bytes(new OtpErlangRef("popa@localhost", 3, 3)));
  }

  @Test
  @DisplayName("decode instance from byte array and compare with jinterface result")
  void decode () throws Exception {
    byte[] bytes1 = Bytes.resizableArray()
        .write1B(REFERENCE.getCode())
        .writeNB(new ErlangAtom("popa@localhost").toBytes())
        .write4B(Integer.MAX_VALUE)
        .write1B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes1)) {
      ErlangReference reference = ErlangTerm.newInstance(wrappedBuffer(bytes1));
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(convert(otpRef.ids()));

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }

    byte[] bytes2 = Bytes.resizableArray()
        .write1B(NEW_REFERENCE.getCode())
        .write2B(3)
        .writeNB(new ErlangAtom("popa@localhost").toBytes())
        .write1B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes2)) {
      ErlangReference reference = ErlangTerm.newInstance(wrappedBuffer(bytes2));
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(convert(otpRef.ids()));

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }

    byte[] bytes3 = Bytes.resizableArray()
        .write1B(NEWER_REFERENCE.getCode())
        .write2B(3)
        .writeNB(new ErlangAtom("popa@localhost").toBytes())
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .write4B(Integer.MAX_VALUE)
        .array();

    try (val input = new OtpInputStream(bytes3)) {
      ErlangReference reference = ErlangTerm.newInstance(wrappedBuffer(bytes3));
      OtpErlangRef otpRef = input.read_ref();

      assertThat(reference.getDescriptor().getFullName())
          .isEqualTo(otpRef.node());

      assertThat(reference.getId())
          .isEqualTo(otpRef.id());

      assertThat(reference.getIds())
          .isEqualTo(convert(otpRef.ids()));

      assertThat(reference.getCreation())
          .isEqualTo(otpRef.creation());
    }
  }

  @SneakyThrows
  private byte[] bytes (OtpErlangRef ref) {
    try (OtpOutputStream output = new OtpOutputStream()) {
      ref.encode(output);
      output.trimToSize();
      return output.toByteArray();
    }
  }

  private long[] convert (int[] ids) {
    long[] result = new long[ids.length];
    for (int index = 0; index < ids.length; index++) {
        result[index] = (long) ids[index];
    }
    return result;
  }
}
