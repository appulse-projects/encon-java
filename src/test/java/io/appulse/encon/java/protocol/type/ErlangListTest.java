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

import static io.appulse.encon.java.protocol.TermType.LIST;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class ErlangListTest {

  @Test
  public void newInstance () {
    val value = new Nil();
    val bytes = Bytes.allocate()
        .put1B(LIST.getCode())
        .put4B(1)
        .put(value.toBytes())
        .put(new Nil().toBytes())
        .array();

    ErlangList list = ErlangTerm.newInstance(bytes);
    assertThat(list).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(list.isContainerTerm())
          .isTrue();

      softly.assertThat(list.isList())
          .isTrue();

      softly.assertThat(list.get(0))
          .isPresent()
          .hasValue(value);

      softly.assertThat(list.size())
          .isEqualTo(1);

      softly.assertThat(list.getTail())
          .isEqualTo(new Nil());
    });
  }

  @Test
  public void toBytes () {
    val value = new Nil();
    val expected = Bytes.allocate()
        .put1B(LIST.getCode())
        .put4B(1)
        .put(value.toBytes())
        .put(new Nil().toBytes())
        .array();

    assertThat(new ErlangList(value).toBytes())
        .isEqualTo(expected);
  }
}
