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

import static io.appulse.encon.java.protocol.control.ControlMessageTag.EXIT2_TT;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.encon.java.protocol.type.Tuple;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
public class Exit2TraceTokenControlMessage extends ExitTraceTokenControlMessage {

  public Exit2TraceTokenControlMessage (@NonNull Pid from, @NonNull Pid to, @NonNull ErlangTerm traceToken, @NonNull ErlangTerm reason) {
    super(from, to, traceToken, reason);
  }

  public Exit2TraceTokenControlMessage (@NonNull Tuple tuple) {
    super(tuple);
  }

  @Override
  public ControlMessageTag getTag () {
    return EXIT2_TT;
  }
}
