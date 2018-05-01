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

package io.appulse.encon.java.terms.term;

import static io.appulse.encon.java.terms.TermType.BINARY;
import static io.appulse.encon.java.terms.TermType.NIL;

import io.appulse.encon.java.terms.TermType;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public interface ValueTerm {

  TermType getType ();

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

  default boolean isNil () {
    return getType() == NIL;
  }

  /**
   * Method that can be used to check if this term represents
   * binary data.
   *
   * @return True if this term represents binary data
   */
  default boolean isBinary () {
    return getType() == BINARY;
  }

  /**
   * Method to use for accessing binary content of binary terms (terms
   * for which {@link #isBinary} returns true);
   * For other types of nodes, returns empty byte array.
   *
   * @return binary data this term contains, if it is a binary term;
   *         empty array otherwise
   */
  default byte[] asBinary () {
    return asBinary(new byte[0]);
  }

  default byte[] asBinary (byte[] defaultValue) {
    return defaultValue;
  }
}
