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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;

/**
 * Serializer interface for converting from user's POJO to {@link ErlangTerm} instance.
 *
 * @param <T> user's type for serialization
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
public interface Serializer<T> {

  /**
   * No operation serializer. What for? Because I can!
   */
  Serializer<Object> NOPE_SERIALIZER = object -> {
    throw new UnsupportedOperationException("No operation serializer");
  };

  /**
   * {@link Byte} number serializer.
   */
  Serializer<Byte> BYTE_SERIALIZER = Erlang::number;

  /**
   * {@link Short} number serializer.
   */
  Serializer<Short> SHORT_SERIALIZER = Erlang::number;

  /**
   * {@link Integer} number serializer.
   */
  Serializer<Integer> INTEGER_SERIALIZER = Erlang::number;

  /**
   * {@link Long} number serializer.
   */
  Serializer<Long> LONG_SERIALIZER = Erlang::number;

  /**
   * {@link BigInteger} number serializer.
   */
  Serializer<BigInteger> BIG_INTEGER_SERIALIZER = Erlang::number;

  /**
   * {@link Float} number serializer.
   */
  Serializer<Float> FLOAT_SERIALIZER = Erlang::number;

  /**
   * {@link Double} number serializer.
   */
  Serializer<Double> DOUBLE_SERIALIZER = Erlang::number;

  /**
   * {@link BigDecimal} number serializer.
   */
  Serializer<BigDecimal> BIG_DECIMAL_SERIALIZER = Erlang::number;

  /**
   * {@link Boolean} serializer.
   */
  Serializer<Boolean> BOOLEAN_SERIALIZER = Erlang::atom;

  /**
   * Atom serializer.
   */
  Serializer<String> ATOM_SERIALIZER = Erlang::atom;

  /**
   * {@link String} serializer.
   */
  Serializer<String> STRING_SERIALIZER = Erlang::string;

  /**
   * {@code byte[]} serializer.
   */
  Serializer<byte[]> BYTE_ARRAY_SERIALIZER = Erlang::binary;

  /**
   * {@link ErlangTerm} serializer.
   */
  Serializer<ErlangTerm> ERLANG_TERM_SERIALIZER = it -> it;

  /**
   * Dictionary of cached {@link Serializer} instances by its types.
   */
  Map<Class<? extends Serializer<?>>, Serializer<?>> SERIALIZERS = new ConcurrentHashMap<>(5);

  /**
   * Exception free new serializer instantiation.
   */
  Function<Class<? extends Serializer<?>>, Serializer<?>> NEW_SERIALIZER = type -> {
    try {
      return type.newInstance();
    } catch (IllegalAccessException | InstantiationException ex) {
      return null;
    }
  };

  /**
   * Returns well-known predefined {@link Serializer} instance by its type or {@code null}.
   *
   * @param type class for search
   *
   * @return {@link Serializer} instance or {@code null}
   */
  static Serializer<?> findInPredefined (Class<?> type) {
    if (ErlangTerm.class.isAssignableFrom(type)) {
      return ERLANG_TERM_SERIALIZER;
    } else if (Byte.class.isAssignableFrom(type) || type == Byte.TYPE) {
      return BYTE_SERIALIZER;
    } else if (Short.class.isAssignableFrom(type) || type == Short.TYPE) {
      return SHORT_SERIALIZER;
    } else if (Integer.class.isAssignableFrom(type) || type == Integer.TYPE) {
      return INTEGER_SERIALIZER;
    } else if (Long.class.isAssignableFrom(type) || type == Long.TYPE) {
      return LONG_SERIALIZER;
    } else if (BigInteger.class.isAssignableFrom(type)) {
      return BIG_INTEGER_SERIALIZER;
    } else if (Float.class.isAssignableFrom(type) || type == Float.TYPE) {
      return FLOAT_SERIALIZER;
    } else if (Double.class.isAssignableFrom(type) || type == Double.TYPE) {
      return DOUBLE_SERIALIZER;
    } else if (BigDecimal.class.isAssignableFrom(type)) {
      return BIG_DECIMAL_SERIALIZER;
    } else if (Boolean.class.isAssignableFrom(type) || type == Boolean.TYPE) {
      return BOOLEAN_SERIALIZER;
    } else if (String.class.isAssignableFrom(type)) {
      return STRING_SERIALIZER;
    } else if (type == byte[].class) {
      return BYTE_ARRAY_SERIALIZER;
    } else {
      return null;
    }
  }

  /**
   * Serializes user's POJO into {@link ErlangTerm} instance.
   *
   * @param object user's POJO for serialization
   *
   * @return {@link ErlangTerm} instance
   */
  ErlangTerm serialize (T object);

  /**
   * Serializes raw Java object into {@link ErlangTerm} instance with casting to T type inside.
   *
   * @param object Java object
   *
   * @return {@link ErlangTerm} instance
   */
  @SuppressWarnings({
      "unchecked",
      "PMD.AvoidThrowingNewInstanceOfSameException",
      "PMD.PreserveStackTrace"
  })
  default ErlangTerm serializeUntyped (Object object) {
    try {
      T casted = (T) object;
      return serialize(casted);
    } catch (ClassCastException ex) {
      String message = String.format("Serializer '%s' couldn't cast value of type '%s' into needed type",
          this.getClass().getSimpleName(),
          object.getClass().getSimpleName()
      );
      throw new ClassCastException(message);
    }
  }
}
