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

import java.util.HashSet;
import java.util.Set;

import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * Field deserializer for set Erlang terms.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class FieldDeserializerSet implements Deserializer<Set<?>> {

  Deserializer<?> elementDeserializer;

  /**
   * Construct set deserializer.
   *
   * @param elementClass set's element type
   */
  public FieldDeserializerSet (@NonNull Class<?> elementClass) {
    elementDeserializer = findInPredefined(elementClass);
  }

  @Override
  public Set<?> deserialize (@NonNull ErlangTerm term) {
    Set<Object> result = new HashSet<>(term.size());
    term.forEach(it -> {
      Object element = elementDeserializer.deserialize(it);
      result.add(element);
    });
    return result;
  }
}
