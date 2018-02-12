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

package io.appulse.encon.java.module.connection.control;

import static io.appulse.encon.java.module.connection.control.ControlMessageTag.SEND;

import io.appulse.encon.java.module.connection.control.exception.ControlMessageParsingException;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangTuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Send extends ControlMessage {

  @NonNull
  ErlangTerm to; // atom or pid

  public Send (@NonNull ErlangTuple tuple) {
    to = tuple.get(2)
        .filter(it -> it.isAtom() || it.isPid())
        .orElseThrow(ControlMessageParsingException::new);
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
