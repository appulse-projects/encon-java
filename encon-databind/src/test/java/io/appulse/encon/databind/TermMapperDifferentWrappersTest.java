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

import static io.appulse.encon.terms.TermType.BINARY;
import static io.appulse.encon.terms.TermType.LIST;
import static io.appulse.encon.terms.TermType.MAP;
import static io.appulse.encon.terms.TermType.SMALL_TUPLE;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import io.appulse.encon.databind.annotation.AsErlangBinary;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangMap;
import io.appulse.encon.databind.annotation.AsErlangTuple;

import java.io.Serializable;

import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@DisplayName("Different term wrappers tests")
@FieldDefaults(level = PRIVATE, makeFinal = true)
class TermMapperDifferentWrappersTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("default wrapper")
  void defaultWrapper () {
    val erlangTerm = TermMapper.serialize(new DefaultWrapper());
    assertThat(erlangTerm.getType())
        .isEqualTo(SMALL_TUPLE);

    assertThat(TermMapper.deserialize(erlangTerm, DefaultWrapper.class))
        .isNotNull();
  }

  @Test
  @DisplayName("list wrapper")
  void listWrapper () {
    val erlangTerm = TermMapper.serialize(new ListWrapper());
    assertThat(erlangTerm.getType())
        .isEqualTo(LIST);

    assertThat(TermMapper.deserialize(erlangTerm, ListWrapper.class))
        .isNotNull();
  }

  @Test
  @DisplayName("tuple wrapper")
  void tupleWrapper () {
    val erlangTerm = TermMapper.serialize(new TupleWrapper());
    assertThat(erlangTerm.getType())
        .isEqualTo(SMALL_TUPLE);

    assertThat(TermMapper.deserialize(erlangTerm, TupleWrapper.class))
        .isNotNull();
  }

  @Test
  @DisplayName("map wrapper")
  void mapWrapper () {
    val erlangTerm = TermMapper.serialize(new MapWrapper());
    assertThat(erlangTerm.getType())
        .isEqualTo(MAP);

    assertThat(TermMapper.deserialize(erlangTerm, MapWrapper.class))
        .isNotNull();
  }

  @Test
  @DisplayName("binary wrapper")
  void binaryWrapper () {
    val erlangTerm = TermMapper.serialize(new BinaryWrapper());
    assertThat(erlangTerm.getType())
        .isEqualTo(BINARY);

    assertThat(TermMapper.deserialize(erlangTerm, BinaryWrapper.class))
        .isNotNull();
  }

  public static class DefaultWrapper {

  }

  @AsErlangList
  public static class ListWrapper {

  }

  @AsErlangTuple
  public static class TupleWrapper {

  }

  @AsErlangMap
  public static class MapWrapper {

  }

  @AsErlangBinary
  public static class BinaryWrapper implements Serializable {

    private static final long serialVersionUID = -4561417197382835517L;

  }
}
