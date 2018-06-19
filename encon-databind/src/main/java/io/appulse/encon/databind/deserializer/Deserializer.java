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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.appulse.encon.terms.ErlangTerm;

/**
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
public interface Deserializer<T> {

  Deserializer<Object> NOPE_DESERIALIZER = new NoOpDeserializer();

  Deserializer<Byte> BYTE_DESERIALIZER = ErlangTerm::asByte;

  Deserializer<Short> SHORT_DESERIALIZER = ErlangTerm::asShort;

  Deserializer<Integer> INTEGER_DESERIALIZER = ErlangTerm::asInt;

  Deserializer<Long> LONG_DESERIALIZER = ErlangTerm::asLong;

  Deserializer<BigInteger> BIG_INTEGER_DESERIALIZER = ErlangTerm::asBigInteger;

  Deserializer<Float> FLOAT_DESERIALIZER = ErlangTerm::asFloat;

  Deserializer<Double> DOUBLE_DESERIALIZER = ErlangTerm::asDouble;

  Deserializer<BigDecimal> BIG_DECIMAL_DESERIALIZER = ErlangTerm::asDecimal;

  Deserializer<Boolean> BOOLEAN_DESERIALIZER = ErlangTerm::asBoolean;

  Deserializer<String> STRING_DESERIALIZER = ErlangTerm::asText;

  Deserializer<byte[]> BYTE_ARRAY_DESERIALIZER = ErlangTerm::asBinary;

  Deserializer<ErlangTerm> ERLANG_TERM_DESERIALIZER = term -> term;

  Map<Class<? extends Deserializer<?>>, Deserializer<?>> DESERIALIZERS = new ConcurrentHashMap<>(5);

  Function<Class<? extends Deserializer<?>>, Deserializer<?>> NEW_DESERIALIZER = type -> {
    try {
      return type.newInstance();
    } catch (IllegalAccessException | InstantiationException ex) {
      return null;
    }
  };

  static Deserializer<?> findInPredefined (Class<?> type) {
    if (ErlangTerm.class.isAssignableFrom(type)) {
      return ERLANG_TERM_DESERIALIZER;
    } else if (Byte.class.isAssignableFrom(type) || type == Byte.TYPE) {
      return BYTE_DESERIALIZER;
    } else if (Short.class.isAssignableFrom(type) || type == Short.TYPE) {
      return SHORT_DESERIALIZER;
    } else if (Integer.class.isAssignableFrom(type) || type == Integer.TYPE) {
      return INTEGER_DESERIALIZER;
    } else if (Long.class.isAssignableFrom(type) || type == Long.TYPE) {
      return LONG_DESERIALIZER;
    } else if (BigInteger.class.isAssignableFrom(type)) {
      return BIG_INTEGER_DESERIALIZER;
    } else if (Float.class.isAssignableFrom(type) || type == Float.TYPE) {
      return FLOAT_DESERIALIZER;
    } else if (Double.class.isAssignableFrom(type) || type == Double.TYPE) {
      return DOUBLE_DESERIALIZER;
    } else if (BigDecimal.class.isAssignableFrom(type)) {
      return BIG_DECIMAL_DESERIALIZER;
    } else if (Boolean.class.isAssignableFrom(type) || type == Boolean.TYPE) {
      return BOOLEAN_DESERIALIZER;
    } else if (String.class.isAssignableFrom(type)) {
      return STRING_DESERIALIZER;
    } else if (byte[].class.isAssignableFrom(type) || type == byte[].class) {
      return BYTE_ARRAY_DESERIALIZER;
    } else {
      return null;
    }
  }

  T deserialize (ErlangTerm term);
}
