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

package io.appulse.encon.connection.control.exception;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public class ControlMessageParsingException extends RuntimeException {

  private static final long serialVersionUID = 6926960910687262514L;

  public static ControlMessageFieldParsingException fieldException (ErlangTuple tuple, int index, Class<?> expectingType) {
    val string = tuple.get(0)
        .map(ErlangTerm::getType)
        .map(TermType::toString)
        .orElse("[null]");

    val message = String.format("Expected tuple[%d] is %s, but was %s",
                                index, expectingType.getSimpleName(), string);

    return new ControlMessageFieldParsingException(message, index, expectingType);
  }

  public ControlMessageParsingException () {
    super();
  }

  public ControlMessageParsingException (String message) {
    super(message);
  }

  public ControlMessageParsingException (String message, Throwable cause) {
    super(message, cause);
  }

  public ControlMessageParsingException (Throwable cause) {
    super(cause);
  }
}
