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
import static io.appulse.encon.java.protocol.TermType.NEW_REFERENCE;

import java.util.stream.IntStream;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class ReferenceTest {

  @Test
  public void instantiate () {
    assertThat(Reference.builder()
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
    val node = "popa";
    val ids = new int[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(ids.length)
        .put(new Atom(node).toBytes())
        .put1B(creation)
        .put4B(ids[0] & 0x3FFFF);

    IntStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::put4B);

    val expected = builder.array();

    Reference reference = ErlangTerm.newInstance(expected);
    assertThat(reference).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(reference.getNode())
          .isEqualTo(node);

      softly.assertThat(reference.getIds())
          .isEqualTo(ids);

      softly.assertThat(reference.getId())
          .isEqualTo(ids[0]);

      softly.assertThat(reference.getCreation())
          .isEqualTo(creation);
    });
  }

  @Test
  public void toBytes () {
    val node = "popa";
    val ids = new int[] { 1, 0, 0 };
    val creation = 42;

    val builder = Bytes.allocate()
        .put1B(NEW_REFERENCE.getCode())
        .put2B(ids.length)
        .put(new Atom(node).toBytes())
        .put1B(creation & 0x3)
        .put4B(ids[0] & 0x3FFFF);

    IntStream.of(ids)
        .skip(1)
        .forEachOrdered(builder::put4B);

    val expected = builder.array();

    assertThat(Reference.builder()
            .node(node)
            .ids(ids)
            .creation(creation)
            .build()
            .toBytes()
        )
        .isEqualTo(expected);
  }
}
