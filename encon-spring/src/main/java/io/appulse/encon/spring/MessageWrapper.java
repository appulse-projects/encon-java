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

package io.appulse.encon.spring;

import static java.util.Optional.of;

import java.lang.reflect.Method;
import java.util.Objects;

import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;

import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
enum MessageWrapper {

  TUPLE,
  LIST,
  NONE,
  UNDEFINED;

  static MessageWrapper from (@NonNull Method method) {
    return of(MatchingCaseMapping.class)
        .map(method::getAnnotation)
        .filter(Objects::nonNull)
        .map(MatchingCaseMapping::value)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(MessageWrapper::guessByString)
        .orElseGet(() -> of(method)
            .map(MessageWrapper::guessByArguments)
            .filter(it -> it != UNDEFINED)
            .orElse(TUPLE)
        );
  }

  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  static MessageWrapper guessByArguments (Method method) {
    if (method.getParameterCount() == 1) {
      Class<?> parameterType = method.getParameterTypes()[0];
      if (parameterType.isAnnotationPresent(AsErlangTuple.class)) {
        return TUPLE;
      } else if (parameterType.isAnnotationPresent(AsErlangList.class)) {
        return LIST;
      }
    }
    return UNDEFINED;
  }

  static MessageWrapper guessByString (String str) {
    if (str == null) {
      return NONE;
    }

    val trimmed = str.trim();
    if (trimmed.isEmpty()) {
      return NONE;
    }

    switch (trimmed.charAt(0)) {
    case '{':
      return TUPLE;
    case '[':
      return LIST;
    default:
      return UNDEFINED;
    }
  }
}
