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
 * Boolean term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface BooleanTerm extends ValueTerm {

  /**
   * Method that can be used to check if this term was created from
   * boolean value (literals "true" and "false").
   *
   * @return is boolean value or not
   */
  default boolean isBoolean () {
    return false;
  }

  /**
   * Method that will try to convert value of this term to a Java <b>boolean</b>.
   * <p>
   * If representation cannot be converted to a boolean value,
   * default value of <b>false</b> will be returned; no exceptions are thrown.
   *
   * @return term's boolean representation
   */
  default boolean asBoolean () {
    return asBoolean(false);
  }

  /**
   * Method that will try to convert value of this term to a Java <b>boolean</b>.
   * <p>
   * If representation cannot be converted to a boolean value,
   * specified <b>defaultValue</b> will be returned; no exceptions are thrown.
   *
   * @param defaultValue default value if there is no boolean representation for the term
   *
   * @return term's boolean representation or default value
   */
  default boolean asBoolean (boolean defaultValue) {
    return defaultValue;
  }
}
