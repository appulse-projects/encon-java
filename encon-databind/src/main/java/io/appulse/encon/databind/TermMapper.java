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

import io.appulse.encon.databind.deserializer.Deserializer;
import io.appulse.encon.databind.parser.PojoDescriptor;
import io.appulse.encon.databind.parser.PojoParser;
import io.appulse.encon.databind.serializer.Serializer;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.val;

/**
 * TermMapper provides functionality for serializing and deserializing
 * Erlang terms, either to and from basic POJOs (Plain Old Java Objects).
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public final class TermMapper {

  /**
   * Method to deserialize {@link ErlangTerm} content into given Java type.
   *
   * @param container term for deserialization
   *
   * @param type      user's POJO class
   *
   * @param <T>       type of return instance
   *
   * @return deserialized object
   */
  @SuppressWarnings("unchecked")
  public static <T> T deserialize (@NonNull ErlangTerm container, @NonNull Class<T> type) {
    if (ErlangTerm.class.isAssignableFrom(type)) {
      return (T) container;
    }

    Deserializer<?> deserializer = Deserializer.findInPredefined(type);
    if (deserializer == null) {
      PojoDescriptor descriptor = PojoParser.parse(type);
      deserializer = descriptor.getDeserializer();
    }
    return (T) deserializer.deserialize(container);
  }

  /**
   * Method to serialize given Java object into {@link ErlangTerm}.
   *
   * @param object user's POJO for serialization
   *
   * @return serialized {@link ErlangTerm} instance
   */
  public static ErlangTerm serialize (@NonNull Object object) {
    if (object instanceof ErlangTerm) {
      return (ErlangTerm) object;
    }
    val type = object.getClass();

    Serializer<?> serializer = Serializer.findInPredefined(type);
    if (serializer == null) {
      PojoDescriptor descriptor = PojoParser.parse(type);
      serializer = descriptor.getSerializer();
    }
    return serializer.serializeUntyped(object);
  }

  private TermMapper () {
  }
}
