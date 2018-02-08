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

package io.appulse.encon.java.protocol.control;

import static io.appulse.encon.java.protocol.control.ControlMessageTag.EXIT_TT;

import io.appulse.encon.java.protocol.control.exception.ControlMessageParsingException;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.encon.java.protocol.type.Tuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Value
@NonFinal
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExitTraceTokenControlMessage extends ControlMessage {

  @NonNull
  Pid from;

  @NonNull
  Pid to;

  @NonNull
  ErlangTerm traceToken;

  @NonNull
  ErlangTerm reason;

  public ExitTraceTokenControlMessage (@NonNull Tuple tuple) {
    from = tuple.get(1)
        .filter(ErlangTerm::isPid)
        .map(ErlangTerm::asPid)
        .orElseThrow(ControlMessageParsingException::new);

    to = tuple.get(2)
        .filter(ErlangTerm::isPid)
        .map(ErlangTerm::asPid)
        .orElseThrow(ControlMessageParsingException::new);

    traceToken = tuple.get(3)
        .orElseThrow(ControlMessageParsingException::new);

    reason = tuple.get(4)
        .orElseThrow(ControlMessageParsingException::new);
  }

  @Override
  public ControlMessageTag getTag () {
    return EXIT_TT;
  }

  @Override
  public ErlangTerm[] elements () {
    return new ErlangTerm[] { from, to, traceToken, reason };
  }
}
