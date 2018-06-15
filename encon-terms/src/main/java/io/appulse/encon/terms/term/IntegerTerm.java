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

package io.appulse.encon.terms.term;

import java.math.BigInteger;

/**
 * Integer term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface IntegerTerm extends NumberTerm {

  /**
   * Tells is this term an integer number or not.
   *
   * @return {@code true} if this object is an integer number.
   */
  default boolean isIntegralNumber () {
    switch (getType()) {
    case INTEGER:
    case SMALL_INTEGER:
    case SMALL_BIG:
    case LARGE_BIG:
      return true;
    default:
      return false;
    }
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code byte}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code byte}
   */
  default boolean isByte () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code short}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code short}
   */
  default boolean isShort () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code int}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code int}
   */
  default boolean isInt () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code long}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code long}
   */
  default boolean isLong () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as {@link BigInteger}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as {@link BigInteger}
   */
  default boolean isBigInteger () {
    return false;
  }

  /**
   * Method that will try to convert value of this term to a Java {@code byte}.
   * <p>
   * If representation cannot be converted to a {@code byte} value,
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code byte} representation
   */
  default byte asByte () {
    return asByte((byte) 0);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code byte}.
   * <p>
   * If representation cannot be converted to a {@code byte} value,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value if there is no {@code byte} representation for the term
   *
   * @return term's {@code byte} representation or default value
   */
  default byte asByte (byte defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java {@code short}.
   * <p>
   * If representation cannot be converted to a {@code short} value,
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code short} representation
   */
  default short asShort () {
    return asShort((short) 0);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code short}.
   * <p>
   * If representation cannot be converted to a {@code short} value,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value if there is no {@code short} representation for the term
   *
   * @return term's {@code short} representation or default value
   */
  default short asShort (short defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java {@code int}.
   * <p>
   * If representation cannot be converted to an {@code int},
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code int} representation
   */
  default int asInt () {
    return asInt(0);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code int}.
   * <p>
   * If representation cannot be converted to an {@code int},
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's {@code int} representation or default value
   */
  default int asInt (int defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java {@code long}.
   * <p>
   * If representation cannot be converted to an {@code long},
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code long} representation
   */
  default long asLong () {
    return asLong(0);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code long}.
   * <p>
   * If representation cannot be converted to an {@code long},
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's {@code long} representation or default value
   */
  default long asLong (long defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a {@link BigInteger}.
   * <p>
   * If representation cannot be converted,
   * default value of {@link BigInteger#ZERO} will be returned, no exceptions are thrown.
   *
   * @return term's {@link BigInteger} representation
   */
  default BigInteger asBigInteger () {
    return asBigInteger(BigInteger.ZERO);
  }

  /**
   * Method that will try to convert value of this term to a {@link BigInteger}.
   * <p>
   * If representation cannot be converted, specified <b>defaultValue</b>
   * will be returned, no exceptions are thrown.
   *
   * @param defaultValue default value if there is no {@link BigInteger} representation for the term
   *
   * @return term's {@link BigInteger} representation or default value
   */
  default BigInteger asBigInteger (BigInteger defaultValue) {
    return defaultValue;
  }
}
