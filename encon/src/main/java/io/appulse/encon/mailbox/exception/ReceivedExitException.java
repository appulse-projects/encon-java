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

package io.appulse.encon.mailbox.exception;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class ReceivedExitException extends RuntimeException {

  private static final long serialVersionUID = 7678315293152032729L;

  ErlangPid from;

  ErlangTerm reason;

  public ReceivedExitException (ErlangPid from, ErlangTerm reason) {
    super(String.format("From: %s, reason: %s", from, reason));
    this.from = from;
    this.reason = reason;
  }
}
