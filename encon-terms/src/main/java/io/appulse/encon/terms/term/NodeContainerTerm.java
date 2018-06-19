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

package io.appulse.encon.terms.term;

import static io.appulse.encon.terms.TermType.NEW_PID;
import static io.appulse.encon.terms.TermType.NEW_PORT;
import static io.appulse.encon.terms.TermType.PID;
import static io.appulse.encon.terms.TermType.PORT;

import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.encon.terms.type.ErlangPort;
import io.appulse.encon.terms.type.ErlangReference;

/**
 * Conteiner term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface NodeContainerTerm extends ValueTerm {

  /**
   * Tells is this term a node container or not.
   * The term "node container" is used as a group name
   * for the Erlang data types that contain a reference
   * to a node, i.e. pids, ports, and references.
   *
   * @return {@code true} if this object is a container.
   */
  @SuppressWarnings("deprecation")
  default boolean isNodeContainerTerm () {
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

  /**
   * Tells is this term a eralng reference type or not.
   *
   * @return {@code true} if this object is a eralng reference type.
   */
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

  /**
   * Method that will try to convert value of this term to a {@link ErlangReference} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangReference} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangReference} representation
   */
  default ErlangReference asReference () {
    return null;
  }

  /**
   * Tells is this term a eralng reference type or not.
   *
   * @return {@code true} if this object is a eralng reference type.
   */
  default boolean isPid () {
    return getType() == PID || getType() == NEW_PID;
  }

  /**
   * Method that will try to convert value of this term to a {@link ErlangPid} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangPid} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangPid} representation
   */
  default ErlangPid asPid () {
    return null;
  }

  /**
   * Tells is this term a eralng port type or not.
   *
   * @return {@code true} if this object is a eralng port type.
   */
  default boolean isPort () {
    return getType() == PORT || getType() == NEW_PORT;
  }

  /**
   * Method that will try to convert value of this term to a {@link ErlangPort} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangPort} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangPort} representation
   */
  default ErlangPort asPort () {
    return null;
  }

  /**
   * Tells is this term a eralng atom type or not.
   *
   * @return {@code true} if this object is a eralng atom type.
   */
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

  /**
   * Method that will try to convert value of this term to a {@link ErlangAtom} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangAtom} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangAtom} representation
   */
  default ErlangAtom asAtom () {
    return null;
  }
}
