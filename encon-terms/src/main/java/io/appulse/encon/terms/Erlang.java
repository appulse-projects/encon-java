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

package io.appulse.encon.terms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import io.appulse.encon.terms.exception.ErlangTermValidationException;
import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.encon.terms.type.ErlangBinary;
import io.appulse.encon.terms.type.ErlangBitString;
import io.appulse.encon.terms.type.ErlangFloat;
import io.appulse.encon.terms.type.ErlangInteger;
import io.appulse.encon.terms.type.ErlangList;
import io.appulse.encon.terms.type.ErlangMap;
import io.appulse.encon.terms.type.ErlangNil;
import io.appulse.encon.terms.type.ErlangString;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.NonNull;

/**
 * Utility class with different static methods for simple constructing terms.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public final class Erlang {

  /**
   * Cached {@link ErlangNil} instance.
   */
  public static final ErlangNil NIL = new ErlangNil();

  /**
   * Cached enpty (0-size string) {@link ErlangAtom} instance.
   */
  public static final ErlangAtom EMPTY_ATOM = ErlangAtom.cached("");

  /**
   * Creates new {@link ErlangAtom} instance from {@code boolean} (true/false value) .
   *
   * @param value atom's value
   *
   * @return {@link ErlangAtom} new instance
   */
  public static ErlangAtom atom (boolean value) {
    return ErlangAtom.cached(value);
  }

  /**
   * Creates new {@link ErlangAtom} instance from {@link String}.
   *
   * @param value atom's value
   *
   * @return {@link ErlangAtom} new instance
   */
  public static ErlangAtom atom (@NonNull String value) {
    return ErlangAtom.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@code char} value.
   *
   * @param value {@code char} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (char value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@code byte} value.
   *
   * @param value {@code byte} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (byte value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@code short} value.
   *
   * @param value {@code short} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (short value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@code int} value.
   *
   * @param value {@code int} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (int value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@code long} value.
   *
   * @param value {@code long} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (long value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangInteger} instance from {@link BigInteger} value.
   *
   * @param value {@link BigInteger} value
   *
   * @return {@link ErlangInteger} new instance
   */
  public static ErlangInteger number (@NonNull BigInteger value) {
    return ErlangInteger.cached(value);
  }

  /**
   * Creates new {@link ErlangFloat} instance from {@code float} value.
   *
   * @param value {@code float} value
   *
   * @return {@link ErlangFloat} new instance
   */
  public static ErlangFloat number (float value) {
    return new ErlangFloat(value);
  }

  /**
   * Creates new {@link ErlangFloat} instance from {@code double} value.
   *
   * @param value {@code double} value
   *
   * @return {@link ErlangFloat} new instance
   */
  public static ErlangFloat number (double value) {
    return new ErlangFloat(value);
  }

  /**
   * Creates new {@link ErlangFloat} instance from {@link BigDecimal} value.
   *
   * @param value {@link BigDecimal} value
   *
   * @return {@link ErlangFloat} new instance
   */
  public static ErlangFloat number (BigDecimal value) {
    return new ErlangFloat(value);
  }

  /**
   * Creates new {@link ErlangBinary} instance from byte array value.
   *
   * @param value byte array value
   *
   * @return {@link ErlangBinary} new instance
   */
  public static ErlangBinary binary (@NonNull byte[] value) {
    return new ErlangBinary(value);
  }

  /**
   * Creates new {@link ErlangBitString} instance.
   *
   * @param bits bitstring's bits
   *
   * @param pad  bitstring's pad
   *
   * @return {@link ErlangBitString} new instance
   */
  public static ErlangBitString bitstr (@NonNull byte[] bits, int pad) {
    return new ErlangBitString(bits, pad);
  }

  /**
   * Creates new {@link ErlangString} instance.
   *
   * @param value string's value
   *
   * @return {@link ErlangString} new instance
   */
  public static ErlangString string (@NonNull String value) {
    // TO-DO: how to determine is it ErlangString, ErlangList or ErlangBitString?
    return new ErlangString(value);
  }

  /**
   * Creates new {@link ErlangBinary} from string with default charset.
   *
   * @param value represent string
   *
   * @return {@link ErlangBinary} new instance
   *
   * @since 1.20
   */
  public static ErlangBinary bstring (String value) {
    return bstring(value, Charset.defaultCharset());
  }

  /**
   * Creates new {@link ErlangBinary} from string with given charset.
   *
   * @param value   represent string
   *
   * @param charset charset
   *
   * @return {@link ErlangBinary} new instance
   *
   * @since 1.20
   */
  public static ErlangBinary bstring (String value, @NonNull String charset) {
    return bstring(value, Charset.forName(charset));
  }

  /**
   * Creates new {@link ErlangBinary} from string with given charset.
   *
   * @param value   represent string
   *
   * @param charset charset
   *
   * @return {@link ErlangBinary} new instance
   *
   * @since 1.20
   */
  public static ErlangBinary bstring (@NonNull String value, @NonNull Charset charset) {
    byte[] bytes = value.getBytes(charset);
    return binary(bytes);
  }

  /**
   * Creates new {@link ErlangTuple} instance.
   *
   * @param elements {@link ErlangTerm} tuple's elements
   *
   * @return {@link ErlangTuple} new instance
   */
  public static ErlangTuple tuple (@NonNull ErlangTerm... elements) {
    return new ErlangTuple(elements);
  }

  /**
   * Creates new {@link ErlangTuple} instance.
   *
   * @param elements {@link ErlangTerm} tuple's elements
   *
   * @return {@link ErlangTuple} new instance
   */
  public static ErlangTuple tuple (@NonNull Collection<ErlangTerm> elements) {
    return new ErlangTuple(elements);
  }

  /**
   * Creates new {@link ErlangList} instance.
   *
   * @param elements {@link ErlangTerm} list's elements
   *
   * @return {@link ErlangList} new instance
   */
  public static ErlangList list (@NonNull ErlangTerm... elements) {
    return new ErlangList(elements);
  }

  /**
   * Creates new {@link ErlangList} instance.
   *
   * @param elements {@link ErlangTerm} list's elements
   *
   * @return {@link ErlangList} new instance
   */
  public static ErlangList list (@NonNull Collection<ErlangTerm> elements) {
    return new ErlangList(elements);
  }

  /**
   * Creates new {@link ErlangList} instance.
   *
   * @param tail     {@link ErlangTerm} list's tail
   *
   * @param elements {@link ErlangTerm} list's elements
   *
   * @return {@link ErlangList} new instance
   */
  public static ErlangList list (@NonNull ErlangTerm tail, @NonNull List<ErlangTerm> elements) {
    return new ErlangList(tail, elements);
  }

  /**
   * Creates new {@link ErlangMap} instance from {@link ErlangTerm} array value.
   *
   * @param keysAndValues {@link ErlangTerm} array of key/value elements. Like: [key1, value1, key2, value2, ...]
   *
   * @return {@link ErlangMap} new instance
   *
   * @throws ErlangTermValidationException in case of not even {@code keysAndValues} length
   */
  public static ErlangMap map (@NonNull ErlangTerm... keysAndValues) {
    if (keysAndValues.length % 2 != 0) {
      throw new ErlangTermValidationException("Keys and Values array must be even");
    }

    LinkedHashMap<ErlangTerm, ErlangTerm> map = new LinkedHashMap<>(keysAndValues.length / 2);
    for (int index = 0; index < keysAndValues.length - 1; index += 2) {
      map.put(keysAndValues[index], keysAndValues[index + 1]);
    }
    return new ErlangMap(map);
  }

  private Erlang () {
  }
}
