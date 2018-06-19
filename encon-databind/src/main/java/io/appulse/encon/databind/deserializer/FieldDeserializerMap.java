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

package io.appulse.encon.databind.deserializer;

import static io.appulse.encon.databind.deserializer.Deserializer.findInPredefined;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.appulse.encon.terms.ErlangTerm;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * Field deserializer for map Erlang terms.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class FieldDeserializerMap implements Deserializer<Map<?, ?>> {

  Deserializer<?> keyDeserializer;

  Deserializer<?> valueDeserializer;

  /**
   * Construct map deserializer.
   *
   * @param keyClass   map's key type
   *
   * @param valueClass map's value type
   */
  @Builder
  public FieldDeserializerMap (@NonNull Class<?> keyClass, @NonNull Class<?> valueClass) {
    keyDeserializer = findInPredefined(keyClass);
    valueDeserializer = findInPredefined(valueClass);
  }

  @Override
  public Map<?, ?> deserialize (@NonNull ErlangTerm term) {
    Map<Object, Object> result = new HashMap<>(term.size());

    Iterator<Map.Entry<ErlangTerm, ErlangTerm>> iterator = term.fields();
    while (iterator.hasNext()) {
      Map.Entry<ErlangTerm, ErlangTerm> entry = iterator.next();

      ErlangTerm keyTerm = entry.getKey();
      Object key = keyDeserializer.deserialize(keyTerm);

      ErlangTerm valueTerm = entry.getValue();
      Object value = valueDeserializer.deserialize(valueTerm);

      result.put(key, value);
    }
    return result;
  }
}
