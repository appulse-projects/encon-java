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

import static io.appulse.encon.connection.control.ControlMessageTag.EXIT2_TT;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.EqualsAndHashCode;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@EqualsAndHashCode(callSuper = true)
public class Exit2TraceToken extends ExitTraceToken {

  public Exit2TraceToken (ErlangPid from, ErlangPid to, ErlangTerm traceToken, ErlangTerm reason) {
    super(from, to, traceToken, reason);
  }

  public Exit2TraceToken (ErlangTuple tuple) {
    super(tuple);
  }

  @Override
  public ControlMessageTag getTag () {
    return EXIT2_TT;
  }
}
