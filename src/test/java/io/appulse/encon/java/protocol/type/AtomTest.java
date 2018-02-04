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
import static io.appulse.encon.java.protocol.TermType.ATOM_UTF8;
import static io.appulse.encon.java.protocol.TermType.SMALL_ATOM_UTF8;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import lombok.val;

public class AtomTest {

  @Test
  public void instantiate () {
    assertThat(new Atom("hello").getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(new Atom(new String(new char[256])).getType())
        .isEqualTo(ATOM_UTF8);

    assertThat(new Atom(true).getType())
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

    Atom atom = ErlangTerm.newInstance(bytes);
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

    assertThat(new Atom(value).toBytes())
        .isEqualTo(expected);
  }
}
