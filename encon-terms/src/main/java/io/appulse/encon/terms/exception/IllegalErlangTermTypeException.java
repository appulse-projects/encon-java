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

package io.appulse.encon.terms.exception;

import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.terms.TermType;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Illegal argument exception of Erlang term object.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class IllegalErlangTermTypeException extends ErlangTermDecodeException {

  private static final long serialVersionUID = -3886868343555057201L;

  Class<?> klass;

  TermType type;

  @Override
  public String getMessage () {
    return new StringBuilder()
        .append("Class ")
        .append(klass.getName())
        .append(" doesn't fit to term type ")
        .append(type)
        .toString();
  }
}
