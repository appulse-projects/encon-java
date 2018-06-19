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

import java.math.BigDecimal;

/**
 * Float term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface FloatTerm extends NumberTerm {

  /**
   * Tells is this term an floating point number or not.
   *
   * @return {@code true} if this object is a floating point number.
   */
  default boolean isFloatingPointNumber () {
    switch (getType()) {
    case FLOAT:
    case NEW_FLOAT:
      return true;
    default:
      return false;
    }
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code float}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code float}
   */
  default boolean isFloat () {
    return isFloatingPointNumber();
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as Java {@code double}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as Java {@code double}
   */
  default boolean isDouble () {
    return isFloatingPointNumber();
  }

  /**
   * Method that can be used to check whether contained value
   * is a number represented as {@link BigDecimal}.
   * Note, however, that even if this method returns false, it
   * is possible that conversion would be possible from other numeric
   * types.
   *
   * @return {@code true} if the value contained by this term is stored as {@link BigDecimal}
   */
  default boolean isBigDecimal () {
    return isFloatingPointNumber();
  }

  /**
   * Method that will try to convert value of this term to a Java {@code float}.
   * <p>
   * If representation cannot be converted to a {@code float},
   * default value of <b>0.0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code float} representation
   */
  default float asFloat () {
    return asFloat(.0F);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code float}.
   * <p>
   * If representation cannot be converted to a {@code float},
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's {@code float} representation
   */
  default float asFloat (float defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a Java {@code double}.
   * <p>
   * If representation cannot be converted to a {@code double},
   * default value of <b>0.0</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@code double} representation
   */
  default double asDouble () {
    return asDouble(.0D);
  }

  /**
   * Method that will try to convert value of this term to a Java {@code double}.
   * <p>
   * If representation cannot be converted to a {@code double},
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's {@code double} representation
   */
  default double asDouble (double defaultValue) {
    return defaultValue;
  }

  /**
   * Method that will try to convert value of this term to a {@link BigDecimal}.
   * <p>
   * If representation cannot be converted to a {@link BigDecimal},
   * default value of <b>{@link BigDecimal#ZERO}</b> will be returned; no exceptions are thrown.
   *
   * @return term's {@link BigDecimal} representation
   */
  default BigDecimal asDecimal () {
    return asDecimal(BigDecimal.ZERO);
  }

  /**
   * Method that will try to convert value of this term to a {@link BigDecimal}.
   * <p>
   * If representation cannot be converted to a {@link BigDecimal},
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value
   *
   * @return term's {@link BigDecimal} representation
   */
  default BigDecimal asDecimal (BigDecimal defaultValue) {
    return defaultValue;
  }
}
