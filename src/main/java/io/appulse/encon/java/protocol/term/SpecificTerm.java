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

package io.appulse.encon.java.protocol.term;

import static io.appulse.encon.java.protocol.TermType.NEW_PID;
import static io.appulse.encon.java.protocol.TermType.NEW_PORT;
import static io.appulse.encon.java.protocol.TermType.PID;
import static io.appulse.encon.java.protocol.TermType.PORT;

import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangPort;
import io.appulse.encon.java.protocol.type.ErlangReference;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public interface SpecificTerm extends ValueTerm {

  @SuppressWarnings("deprecation")
  default boolean isSpecificTerm () {
    switch (getType()) {
    case REFERENCE:
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
    case PID:
    case NEW_PID:
    case PORT:
    case NEW_PORT:
    case ATOM:
    case SMALL_ATOM:
    case ATOM_UTF8:
    case SMALL_ATOM_UTF8:
      return true;
    default:
      return false;
    }
  }

  default boolean isReference () {
    switch (getType()) {
    case REFERENCE:
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      return true;
    default:
      return false;
    }
  }

  default ErlangReference asReference () {
    return null;
  }

  default boolean isPid () {
    return getType() == PID || getType() == NEW_PID;
  }

  default ErlangPid asPid () {
    return null;
  }

  default boolean isPort () {
    return getType() == PORT || getType() == NEW_PORT;
  }

  default ErlangPort asPort () {
    return null;
  }

  @SuppressWarnings("deprecation")
  default boolean isAtom () {
    switch (getType()) {
    case ATOM:
    case SMALL_ATOM:
    case ATOM_UTF8:
    case SMALL_ATOM_UTF8:
      return true;
    default:
      return false;
    }
  }

  default ErlangAtom asAtom () {
    return null;
  }
}
