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

package io.appulse.encon.connection.control;

import static io.appulse.encon.connection.control.ControlMessageTag.SEND;

import io.appulse.encon.connection.control.exception.ControlMessageParsingException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Send extends ControlMessage {

  @NonNull
  ErlangTerm to; // atom or pid

  public Send (@NonNull ErlangTuple tuple) {
    super();

    to = tuple.getUnsafe(2);
    if (to == null || !(to.isAtom() || to.isPid())) {
      throw new ControlMessageParsingException();
    }
  }

  @Override
  public ControlMessageTag getTag () {
    return SEND;
  }

  @Override
  public ErlangTerm[] elements () {
    return new ErlangTerm[] { UNUSED, to };
  }
}
