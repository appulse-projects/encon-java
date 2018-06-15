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

import static io.appulse.encon.terms.TermType.BINARY;
import static io.appulse.encon.terms.TermType.NIL;

import io.appulse.encon.terms.TermType;

/**
 * Basic term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface ValueTerm {

  /**
   * Returns term's type.
   *
   * @return {@link TermType} instance
   */
  TermType getType ();

  /**
   * Tells is this a single value container or not.
   * <p>
   * Value types which are <b>not</b> value containers:
   * <p>
   * <ul>
   * <li>MAP</li>
   * <li>LIST</li>
   * <li>EXTERNAL_FUNCTION</li>
   * <li>FUNCTION</li>
   * <li>COMPRESSED</li>
   * <li>SMALL_TUPLE</li>
   * <li>NEW_FUNCTION</li>
   * <li>LARGE_TUPLE</li>
   * </ul>
   *
   * @return {@code true} if this object is a single value, {@code false} otherwise
   */
  default boolean isValueTerm () {
    switch (getType()) {
    case MAP:
    case LIST:
    case EXTERNAL_FUNCTION:
    case FUNCTION:
    case COMPRESSED:
    case SMALL_TUPLE:
    case NEW_FUNCTION:
    case LARGE_TUPLE:
      return false;
    default:
      return true;
    }
  }

  /**
   * Tells is this a {@link TermType#NIL} type or not.
   *
   * @return {@code true} if this object is a {@link TermType#NIL} type, {@code false} otherwise
   */
  default boolean isNil () {
    return getType() == NIL;
  }

  /**
   * Method that can be used to check if this term represents binary data or not.
   *
   * @return {@code true} if this term represents binary data
   */
  default boolean isBinary () {
    return getType() == BINARY;
  }

  /**
   * Method to use for accessing binary content of binary terms (terms for which {@link #isBinary} returns true).
   * For other types of nodes, returns empty byte array.
   *
   * @return binary data this term contains, if it is a binary term, empty array otherwise
   */
  default byte[] asBinary () {
    return asBinary(new byte[0]);
  }

  /**
   * Method to use for accessing binary content of binary terms (terms for which {@link #isBinary} returns true) or
   * {@code defaultValue} otherwise.
   *
   * @param defaultValue default value if real value is empty
   *
   * @return binary data this term contains, if it is a binary term, empty array otherwise
   */
  default byte[] asBinary (byte[] defaultValue) {
    return defaultValue;
  }
}
