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

import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.Stream;

import io.appulse.encon.terms.ErlangTerm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Slf4j
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class MethodMatcher {

  private static void debug (String message, Object... arguments) {
    if (log.isDebugEnabled()) {
      log.debug(message, arguments);
    }
  }

  @NonNull
  MethodArgumentsWrapper wrapper;

  @NonNull
  MethodArgumentMatcher[] matchers;

  @Getter(value = PRIVATE, lazy = true)
  String toString = createToString();

  boolean matches (ErlangTerm term) {
    debug("matching {} with\n wrapper {} and types: {}", term, wrapper, matchers);

    if (!wrapper.isApplicable(term)) {
      debug("matcher's wrapper {} is not compatible with term type {}", wrapper, term.getType());
      return false;
    }

    if (matchers.length != term.size()) {
      debug("number of method's arguments is {}, but term size is {}",
            matchers.length, term.size());
      return false;
    }

    for (int index = 0; index < matchers.length; index++) {
      val element = term.getUnsafe(index);
      val matcher = matchers[index];
      if (!matcher.matches(element)) {
        debug("incompatible term element {} with argument matcher {} at index {}",
              element, matchers[index], index);
        return false;
      }
    }

    debug("matches");
    return true;
  }

  @Override
  public String toString () {
    return getToString();
  }

  private String createToString () {
    return new StringBuilder()
        .append(wrapper).append('\n')
        .append(Stream.of(matchers)
            .map(Object::toString)
            .collect(joining("\n  - ", "  - ", "")))
        .append(')')
        .toString();
  }
}
