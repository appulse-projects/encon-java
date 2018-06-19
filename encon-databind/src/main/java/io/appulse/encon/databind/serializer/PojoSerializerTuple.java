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

package io.appulse.encon.databind.serializer;

import static io.appulse.encon.terms.Erlang.tuple;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import io.appulse.encon.databind.parser.FieldDescriptor;
import io.appulse.encon.terms.ErlangTerm;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * POJO's serializer into Erlang's tuple.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PojoSerializerTuple extends PojoSerializerAbstractCollection<Object> {

  List<FieldDescriptor> fields;

  @Override
  public ErlangTerm serialize (Object object) {
    val elements = fields.stream()
        .map(it -> serialize(it, object))
        .toArray(ErlangTerm[]::new);

    return tuple(elements);
  }
}
