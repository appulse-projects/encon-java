/*
 * Copyright 2020 the original author or authors.
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

import static io.appulse.encon.connection.control.ControlMessageTag.UNLINK;

import io.appulse.encon.connection.control.exception.ControlMessageParsingException;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Unlink control message.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Unlink extends ControlMessage {

  @NonNull
  ErlangPid from;

  @NonNull
  ErlangPid to;

  public Unlink (@NonNull ErlangTuple tuple) {
    super();

    val tuple1 = tuple.getUnsafe(1);
    if (tuple1 == null || !tuple1.isPid()) {
      throw new ControlMessageParsingException();
    }
    from = tuple1.asPid();

    val tuple2 = tuple.getUnsafe(2);
    if (tuple2 == null || !tuple2.isPid()) {
      throw new ControlMessageParsingException();
    }
    to = tuple2.asPid();
  }

  @Override
  public ControlMessageTag getTag () {
    return UNLINK;
  }

  @Override
  public ErlangTerm[] elements () {
    return new ErlangTerm[] { from, to };
  }
}
