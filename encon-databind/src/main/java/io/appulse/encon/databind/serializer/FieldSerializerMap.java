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

import static io.appulse.encon.databind.serializer.Serializer.findInPredefined;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangMap;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * Field serializer for map Erlang terms.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class FieldSerializerMap implements Serializer<Map<Object, Object>> {

  Serializer keySerializer;

  Serializer valueSerializer;

  /**
   * Construct map serializer.
   *
   * @param keyClass   map's key type
   *
   * @param valueClass map's value type
   */
  @Builder
  public FieldSerializerMap (@NonNull Class<?> keyClass, @NonNull Class<?> valueClass) {
    keySerializer = findInPredefined(keyClass);
    valueSerializer = findInPredefined(valueClass);
  }

  @Override
  public ErlangTerm serialize (Map<Object, Object> map) {
    LinkedHashMap<ErlangTerm, ErlangTerm> termMap = map.entrySet()
        .stream()
        .map(it -> new SimpleEntry<>(
            keySerializer.serialize(it.getKey()),
            valueSerializer.serialize(it.getValue())
        ))
        .collect(toMap(
            Entry::getKey,
            Entry::getValue,
            null,
            LinkedHashMap::new
        ));

    return new ErlangMap(termMap);
  }
}
