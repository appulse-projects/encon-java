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

import static io.appulse.encon.databind.annotation.TermContainer.Container.TUPLE;
import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.list;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;

import io.appulse.encon.databind.annotation.TermContainer;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangMap;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class TermMapping {

  static <T> BinaryOperator<T> throwingMerger () {
    return (oldValue, newValue) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", newValue));
    };
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public <T> T deserialize (@NonNull ErlangTerm container, @NonNull Class<T> type) {
    if (ErlangTerm.class.isAssignableFrom(type)) {
      return (T) container;
    }

    T object = type.newInstance();

    if (container.isTuple() || container.isList()) {
      val iterator = container.elements();
      for (val descriptor : PojoParser.parse(type)) {
        if (!iterator.hasNext()) {
          throw new RuntimeException();
        }

        val value = descriptor.getDeserializer()
            .deserialize(iterator.next());

        descriptor.getField().set(object, value);
      }

      if (iterator.hasNext()) {
        throw new RuntimeException();
      }
    } else if (container.isMap()) {
      val iterator = container.fields();
      for (val descriptor : PojoParser.parse(type)) {
        if (!iterator.hasNext()) {
          throw new RuntimeException();
        }

        val entry = iterator.next();
        val key = entry.getKey().asText();
        if (!key.equals(descriptor.getName())) {
          throw new RuntimeException();
        }

        val value = descriptor.getDeserializer()
            .deserialize(entry.getValue());

        descriptor.getField().set(object, value);
      }

      if (iterator.hasNext()) {
        throw new RuntimeException();
      }
    } else {
      throw new UnsupportedOperationException("Unsupported container type " + container.getType());
    }
    return object;
  }

  @SneakyThrows
  public ErlangTerm serialize (@NonNull Object object) {
    if (object instanceof ErlangTerm) {
      return (ErlangTerm) object;
    }

    val type = object.getClass();

    val terms = PojoParser.parse(type)
        .stream()
        .collect(toMap(
            FieldDescriptor::getName,
            it -> serializeField(it, object),
            throwingMerger(),
            LinkedHashMap::new
        ));

    val container = ofNullable(type.getAnnotation(TermContainer.class))
        .map(TermContainer::value)
        .orElse(TUPLE);

    switch (container) {
    case TUPLE:
      return tuple(terms.values());
    case LIST:
      return list(terms.values());
    case MAP:
      val map = terms.entrySet()
          .stream()
          .collect(toMap(
              it -> (ErlangTerm) atom(it.getKey()),
              Entry::getValue,
              throwingMerger(),
              LinkedHashMap::new
          ));

      return new ErlangMap(map);
    default:
      throw new UnsupportedOperationException("Unsupported container type " + container);
    }
  }

  @SneakyThrows
  private ErlangTerm serializeField (FieldDescriptor descriptor, Object object) {
    val field = descriptor.getField();
    val value = field.get(object);
    return descriptor.getSerializer()
        .serialize(value);
  }
}
