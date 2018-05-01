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

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public interface NumberTerm extends ValueTerm {

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
   * Returns numeric value for this node, <b>if and only if</b>
   * this node is numeric ({@link #isNumber} returns true); otherwise
   * returns null
   *
   * @return Number value this node contains, if any (null for non-number
   *     nodes).
   */
  default Number asNumber () {
    return null;
  }

  default boolean isSigned () {
    return false;
  }
}
