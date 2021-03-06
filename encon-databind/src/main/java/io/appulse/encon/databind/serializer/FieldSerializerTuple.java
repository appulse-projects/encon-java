/*
 * Copyright 2020 the original author or authors.
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
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.stream.Stream;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Field serializer for tuples Erlang terms.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class FieldSerializerTuple implements Serializer<Object> {

  Serializer<?> elementDeserializer;

  /**
   * Construct tuple serializer.
   *
   * @param elementClass collection's element type
   */
  public FieldSerializerTuple (@NonNull Class<?> elementClass) {
    elementDeserializer = findInPredefined(elementClass);
  }

  @Override
  public ErlangTerm serialize (Object object) {
    if (object.getClass().isArray()) {
      return serialize((Object[]) object);
    } else if (object instanceof Collection) {
      return serialize((Collection<Object>) object);
    }
    throw new UnsupportedOperationException("Not object type " + object.getClass());
  }

  /**
   * Serializes <b>array</b> of Java objects to tuple Erlang term.
   *
   * @param array Java POJOs
   *
   * @return serialized {@link ErlangTerm} instance
   */
  public ErlangTerm serialize (Object[] array) {
    if (array.length == 0) {
      return new ErlangTuple();
    }

    val elements = Stream.of(array)
        .map(elementDeserializer::serializeUntyped)
        .toArray(ErlangTerm[]::new);

    return Erlang.tuple(elements);
  }

  /**
   * Serializes <b>{@link Collection}</b> of Java objects to tuple Erlang term.
   *
   * @param collection Java POJOs
   *
   * @return serialized {@link ErlangTerm} instance
   */
  public ErlangTerm serialize (Collection<Object> collection) {
    val array = collection.toArray(new Object[0]);
    return serialize(array);
  }
}
