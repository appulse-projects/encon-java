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

import static io.appulse.encon.terms.Erlang.atom;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BinaryOperator;

import io.appulse.encon.databind.parser.FieldDescriptor;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangMap;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * POJO's serializer into Erlang's map.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PojoSerializerMap extends PojoSerializerAbstractCollection<Object> {

  private static <T> BinaryOperator<T> throwingMerger () {
    return (oldValue, newValue) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", newValue));
    };
  }

  List<FieldDescriptor> fields;

  @Override
  public ErlangTerm serialize (Object object) {
    val termsMap = fields.stream()
        .collect(toMap(
            it -> atom(it.getName()),
            it -> serialize(it, object),
            throwingMerger(),
            LinkedHashMap::new
        ));

    return new ErlangMap(termsMap);
  }

}
