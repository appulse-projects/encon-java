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

package io.appulse.encon.databind;

import static io.appulse.encon.terms.TermType.LIST;
import static io.appulse.encon.terms.TermType.SMALL_ATOM_UTF8;
import static io.appulse.encon.terms.TermType.SMALL_INTEGER;
import static io.appulse.encon.terms.TermType.STRING;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangAtom;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.IgnoreField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.Test;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class TermMapperTest {

  @Test
  public void simple () {
    val pojo = new Pojo(
        "Artem",
        27,
        true,
        539,
        asList("java", "nim", "elixir"),
        "developer",
        singleton("popa"),
        "some long string...or not",
        new Boolean[] { true, false, true }
    );

    val erlangTerm = TermMapper.serialize(pojo);
    assertThat(erlangTerm.getType())
        .isEqualTo(LIST);

    assertThat(erlangTerm.size())
        .isEqualTo(8);

    assertThat(erlangTerm.getUnsafe(0).getType())
        .isEqualTo(STRING);

    assertThat(erlangTerm.getUnsafe(1).getType())
        .isEqualTo(SMALL_INTEGER);

    assertThat(erlangTerm.getUnsafe(2).getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(erlangTerm.getUnsafe(3).getType())
        .isEqualTo(LIST);

    assertThat(erlangTerm.getUnsafe(4).getType())
        .isEqualTo(SMALL_ATOM_UTF8);

    assertThat(erlangTerm.getUnsafe(5).getType())
        .isEqualTo(LIST);

    assertThat(erlangTerm.getUnsafe(6).getType())
        .isEqualTo(LIST);

    assertThat(erlangTerm.getUnsafe(7).getType())
        .isEqualTo(LIST);

    val result = TermMapper.deserialize(erlangTerm, Pojo.class);
    assertThat(pojo == result)
        .isFalse();
    assertThat(result)
        .isEqualTo(pojo);
    assertThat(pojo.getListString())
        .isEqualTo(result.getListString());
    assertThat(pojo.getIgnored())
        .isNotEqualTo(result.getIgnored());
  }

  @Data
  @AsErlangList
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = PRIVATE)
  @EqualsAndHashCode(exclude = "ignored")
  public static class Pojo {

    String name;

    int age;

    boolean male;

    @IgnoreField
    int ignored;

    List<String> languages;

    @AsErlangAtom
    String position;

    @AsErlangList
    Set<String> set;

    @AsErlangList
    String listString;

    @AsErlangList
    Boolean[] bools;
  }
}
