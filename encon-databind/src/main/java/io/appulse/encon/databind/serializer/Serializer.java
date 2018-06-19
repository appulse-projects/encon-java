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
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
public interface Serializer<T> {

  Serializer<Object> NOPE_SERIALIZER = new NoOpSerializer();

  Serializer<Byte> BYTE_SERIALIZER = Erlang::number;

  Serializer<Short> SHORT_SERIALIZER = Erlang::number;

  Serializer<Integer> INTEGER_SERIALIZER = Erlang::number;

  Serializer<Long> LONG_SERIALIZER = Erlang::number;

  Serializer<BigInteger> BIG_INTEGER_SERIALIZER = Erlang::number;

  Serializer<Float> FLOAT_SERIALIZER = Erlang::number;

  Serializer<Double> DOUBLE_SERIALIZER = Erlang::number;

  Serializer<BigDecimal> BIG_DECIMAL_SERIALIZER = Erlang::number;

  Serializer<Boolean> BOOLEAN_SERIALIZER = Erlang::atom;

  Serializer<String> ATOM_SERIALIZER = Erlang::atom;

  Serializer<String> STRING_SERIALIZER = Erlang::string;

  Serializer<byte[]> BYTE_ARRAY_SERIALIZER = Erlang::binary;

  Serializer<ErlangTerm> ERLANG_TERM_SERIALIZER = it -> it;

  Map<Class<? extends Serializer<?>>, Serializer<?>> SERIALIZERS = new ConcurrentHashMap<>(5);

  Function<Class<? extends Serializer<?>>, Serializer<?>> NEW_SERIALIZER = type -> {
    try {
      return type.newInstance();
    } catch (IllegalAccessException | InstantiationException ex) {
      return null;
    }
  };

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

  ErlangTerm serialize (T object);

  @SuppressWarnings("unchecked")
  default ErlangTerm serializeUntyped (Object object) {
    T casted = (T) object;
    return serialize(casted);
  }
}
