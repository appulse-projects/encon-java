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

import java.util.stream.IntStream;

import static io.appulse.encon.java.protocol.TermType.LARGE_TUPLE;
import static io.appulse.encon.java.protocol.TermType.SMALL_TUPLE;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class TupleTest {

  @Test
  public void instantiate () {
    assertThat(new Tuple(new Nil()).getType())
        .isEqualTo(SMALL_TUPLE);

    val elements = IntStream.range(0, 257)
        .boxed()
        .map(it -> new Nil())
        .toArray(ErlangTerm[]::new);

    assertThat(new Tuple(elements).getType())
        .isEqualTo(LARGE_TUPLE);
  }

  @Test
  public void newInstance () {
    val value = new Nil();
    val bytes = Bytes.allocate()
        .put1B(SMALL_TUPLE.getCode())
        .put1B(1)
        .put(value.toBytes())
        .array();

    Tuple tuple = ErlangTerm.newInstance(bytes);
    assertThat(tuple).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(tuple.isContainerTerm())
          .isTrue();

      softly.assertThat(tuple.isTuple())
          .isTrue();

      softly.assertThat(tuple.get(0))
          .isPresent()
          .hasValue(value);

      softly.assertThat(tuple.size())
          .isEqualTo(1);
    });
  }

  @Test
  public void toBytes () {
    val value = new Nil();
    val expected = Bytes.allocate()
        .put1B(SMALL_TUPLE.getCode())
        .put1B(1)
        .put(value.toBytes())
        .array();

    assertThat(new Tuple(value).toBytes())
        .isEqualTo(expected);
  }
}
