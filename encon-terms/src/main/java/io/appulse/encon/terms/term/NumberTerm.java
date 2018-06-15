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

/**
 * Common number term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface NumberTerm extends ValueTerm {

  /**
   * Tells is this term a number or not.
   *
   * @return {@code true} if this object is a number.
   */
  default boolean isNumber () {
    switch (getType()) {
    case INTEGER:
    case SMALL_INTEGER:
    case FLOAT:
    case NEW_FLOAT:
    case SMALL_BIG:
    case LARGE_BIG:
      return true;
    default:
      return false;
    }
  }

  /**
   * Method that will try to convert value of this term to a {@link Number} value.
   * <p>
   * If representation cannot be converted to a {@link Number} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link Number} representation
   */
  default Number asNumber () {
    return null;
  }

  /**
   * Tells is this term a signed number or not.
   *
   * @return {@code true} if this object is a signed number.
   */
  default boolean isSigned () {
    return false;
  }
}
