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

package io.appulse.encon.examples.databind;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangAtom;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.IgnoreField;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
@Data
@Builder
@AsErlangList // Serialize/deserialize object to/from Erlang's list
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(exclude = {
    "sender",
    "ignored"
})
public class Pojo {

  ErlangPid sender;

  // it will be serialized as ErlangString
  String name;

  // it will be serialized as ErlangInteger
  int age;

  // it will be serialized as ErlangAtom
  boolean male;

  // this field will be ignored
  @IgnoreField
  int ignored;

  // it will be serialized as ErlangList with ErlangString elements
  List<String> languages;

  // this field will be serialized as ErlangAtom, obviously
  @AsErlangAtom
  String position;

  // it will be serialized as ErlangList with ErlangString elements
  @AsErlangList
  Set<String> set;

  // this string will be serialized as ErlangList with ErlangInteger elements
  @AsErlangList
  String listString;

  // it will be serialized as ErlangList with ErlangAtom elements
  @AsErlangList
  Boolean[] bools;
}
