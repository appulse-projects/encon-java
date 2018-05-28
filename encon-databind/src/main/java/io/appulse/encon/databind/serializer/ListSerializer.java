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
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;

import java.util.Collection;

import io.appulse.encon.terms.type.ErlangList;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ListSerializer implements Serializer<Object> {

  Serializer<?> elementDeserializer;

  public ListSerializer (@NonNull Class<?> elementClass) {
    elementDeserializer = findInPredefined(elementClass);
  }

  @Override
  public ErlangTerm serialize (Object object) {
    if (object.getClass().isArray()) {
      return serialize((Object[]) object);
    } else if (object instanceof Collection) {
      return serialize((Collection<Object>) object);
    } else if (object instanceof String) {
      return serialize(object.toString());
    }
    throw new UnsupportedOperationException("Not object type " + object.getClass());
  }

  public ErlangTerm serialize (Object[] array) {
    if (array.length == 0) {
      return new ErlangList();
    }

    val elements = Stream.of(array)
        .map(elementDeserializer::serializeUntyped)
        .toArray(ErlangTerm[]::new);

    return Erlang.list(elements);
  }

  public ErlangTerm serialize (Collection<Object> collection) {
    val array = collection.toArray(new Object[0]);
    return serialize(array);
  }

  public ErlangTerm serialize (String string) {
    val array = string.codePoints()
        .boxed()
        .toArray(Object[]::new);

    return serialize(array);
  }
}
