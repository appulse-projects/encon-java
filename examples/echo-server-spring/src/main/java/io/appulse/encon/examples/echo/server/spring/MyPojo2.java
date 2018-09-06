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

package io.appulse.encon.examples.echo.server.spring;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangAtom;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
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
 * @since 1.6.0
 * @author Artem Labazin
 */
@Data
@Builder
@AsErlangTuple
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(exclude = {
    "sender",
    "ignored"
})
public class MyPojo2 {

  ErlangPid sender;

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
