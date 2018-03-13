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

import java.math.BigDecimal;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public interface FloatTerm extends NumberTerm {

  default boolean isFloatingPointNumber () {
    switch (getType()) {
    case FLOAT:
    case NEW_FLOAT:
      return true;
    default:
      return false;
    }
  }

  default boolean isFloat () {
    return isFloatingPointNumber();
  }

  default boolean isDouble () {
    return isFloatingPointNumber();
  }

  default boolean isBigDecimal () {
    return isFloatingPointNumber();
  }

  default float asFloat () {
    return asFloat(.0F);
  }

  default float asFloat (float defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java <b>double</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
   * and 1.0 (true), and Strings are parsed using default Java language double
   * parsing rules.
   * <p>
   * If representation cannot be converted to a double,
   * default value of <b>0.0</b> will be returned; no exceptions are thrown.
   *
   * @return term's double representation
   */
  default double asDouble () {
    return asDouble(.0D);
  }

  /**
   * Method that will try to convert value of this term to a Java <b>double</b>.
   * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
   * and 1.0 (true), and Strings are parsed using default Java language integer
   * parsing rules.
   * <p>
   * If representation cannot be converted to a double,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's double representation
   */
  default double asDouble (double defaultValue) {
    return defaultValue;
  }

  default BigDecimal asDecimal () {
    return asDecimal(BigDecimal.ZERO);
  }

  default BigDecimal asDecimal (BigDecimal defaultValue) {
    return defaultValue;
  }
}
