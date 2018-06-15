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
 * String term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface StringTerm extends ValueTerm {

  /**
   * Method that checks whether this node represents {@link String} value.
   *
   * @return could the term be represented as text or not
   */
  @SuppressWarnings("deprecation")
  default boolean isTextual () {
    switch (getType()) {
    case ATOM:
    case SMALL_ATOM:
    case ATOM_UTF8:
    case SMALL_ATOM_UTF8:
    case BIT_BINNARY:
    case STRING:
      return true;
    default:
      return false;
    }
  }

  /**
   * Method that will return a valid String representation of
   * the container value, otherwise empty String.
   *
   * @return term's string representation
   */
  default String asText () {
    return asText("");
  }

  /**
   * Method similar to {@link #asText}, except that it will return
   * <code>defaultValue</code> in cases where null value would be returned;
   * either for missing nodes (trying to access missing property, or element
   * at invalid item for array) or explicit nulls.
   *
   * @param defaultValue default value
   *
   * @return term's string representation
   */
  default String asText (String defaultValue) {
    return defaultValue;
  }
}
