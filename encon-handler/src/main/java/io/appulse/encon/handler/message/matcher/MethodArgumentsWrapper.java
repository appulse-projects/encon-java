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

package io.appulse.encon.handler.message.matcher;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;

import lombok.val;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
enum MethodArgumentsWrapper {

  TUPLE,
  LIST,
  MAP,
  NONE,
  UNDEFINED;

  static MethodArgumentsWrapper of (TermType type) {
    if (type == null) {
      return UNDEFINED;
    }

    switch (type) {
    case LIST:
      return LIST;
    case MAP:
      return MAP;
    case SMALL_TUPLE:
    case LARGE_TUPLE:
      return TUPLE;
    default:
      return NONE;
    }
  }

  boolean isApplicable (ErlangTerm term) {
    val self = values()[ordinal()];
    switch (self) {
    case LIST:
      return term.isList();
    case TUPLE:
      return term.isTuple();
    case MAP:
      return term.isMap();
    case NONE:
      return true;
    default:
      return false;
    }
  }
}
