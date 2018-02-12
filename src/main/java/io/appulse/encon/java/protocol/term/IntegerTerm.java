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

package io.appulse.encon.java.protocol.term;

import java.math.BigInteger;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public interface IntegerTerm extends NumberTerm {

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
   * is a number represented as Java <code>short</code>.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return True if the value contained by this term is stored as Java short
   */
  default boolean isShort () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java <code>int</code>.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return True if the value contained by this term is stored as Java <code>int</code>
   */
  default boolean isInt () {
    return false;
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java <code>long</code>.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return True if the value contained by this term is stored as Java <code>long</code>
   */
  default boolean isLong () {
    return false;
  }

  default boolean isBigInteger () {
    return false;
  }

  default short asShort () {
    return asShort((short) 0);
  }

  default short asShort (short defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java <b>int</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0 (false)
   * and 1 (true), and Strings are parsed using default Java language integer
   * parsing rules.
   * <p>
   * If representation cannot be converted to an int ,
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's integer representation
   */
  default int asInt () {
    return asInt(0);
  }

  /**
   * Method that will try to convert value of this term to a Java <b>int</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0 (false)
   * and 1 (true), and Strings are parsed using default Java language integer
   * parsing rules.
   * <p>
   * If representation cannot be converted to an int,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's integer representation or default value
   */
  default int asInt (int defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java <b>long</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0 (false)
   * and 1 (true), and Strings are parsed using default Java language integer
   * parsing rules.
   * <p>
   * If representation cannot be converted to an long,
   * default value of <b>0</b> will be returned; no exceptions are thrown.
   *
   * @return term's long representation
   */
  default long asLong () {
    return asLong(0);
  }

  /**
   * Method that will try to convert value of this term to a Java <b>long</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0 (false)
   * and 1 (true), and Strings are parsed using default Java language integer
   * parsing rules.
   * <p>
   * If representation cannot be converted to an long,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's long representation or default value
   */
  default long asLong (long defaultValue) {
    return defaultValue;
  }

  default BigInteger asBigInteger () {
    return asBigInteger(BigInteger.ZERO);
  }

  default BigInteger asBigInteger (BigInteger defaultValue) {
    return defaultValue;
  }
}
