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

import static io.appulse.encon.handler.message.matcher.Matchers.any;
import static io.appulse.encon.handler.message.matcher.Matchers.anyBoolean;
import static io.appulse.encon.handler.message.matcher.Matchers.anyByte;
import static io.appulse.encon.handler.message.matcher.Matchers.anyChar;
import static io.appulse.encon.handler.message.matcher.Matchers.anyCollection;
import static io.appulse.encon.handler.message.matcher.Matchers.anyDouble;
import static io.appulse.encon.handler.message.matcher.Matchers.anyFloat;
import static io.appulse.encon.handler.message.matcher.Matchers.anyInt;
import static io.appulse.encon.handler.message.matcher.Matchers.anyIterable;
import static io.appulse.encon.handler.message.matcher.Matchers.anyList;
import static io.appulse.encon.handler.message.matcher.Matchers.anyLong;
import static io.appulse.encon.handler.message.matcher.Matchers.anyMap;
import static io.appulse.encon.handler.message.matcher.Matchers.anySet;
import static io.appulse.encon.handler.message.matcher.Matchers.anyShort;
import static io.appulse.encon.handler.message.matcher.Matchers.anyString;
import static io.appulse.encon.handler.message.matcher.Matchers.eq;
import static io.appulse.encon.spring.MessageWrapper.NONE;
import static java.util.Optional.empty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
import io.appulse.encon.databind.annotation.IgnoreField;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(of = {
    "patternArguments",
    "wrapper"
})
@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
class ControllerMethodDescriptor {

  static ControllerMethodDescriptor of (Method method) {
    MessageWrapper wrapper = MessageWrapper.from(method);
    val arguments = createArguments(method, wrapper);
    return new ControllerMethodDescriptor(method, arguments, wrapper);
  }

  @NonNull
  Method method;

  @NonNull
  Object[] patternArguments;

  @NonNull
  MessageWrapper wrapper;

  private static Object[] createArguments (Method method, MessageWrapper wrapper) {
    if (method.getParameterCount() == 1) {
      Class<?> type = method.getParameterTypes()[0];
      if (type.isAnnotationPresent(AsErlangList.class) ||
          type.isAnnotationPresent(AsErlangTuple.class)) {

        return Stream.of(type.getDeclaredFields())
            .filter(it -> !it.isAnnotationPresent(IgnoreField.class))
            .map(Field::getType)
            .map(it -> any())
            .toArray(Object[]::new);
      }
    }

    val arguments = Optional.of(MatchingCaseMapping.class)
        .map(method::getAnnotation)
        .filter(Objects::nonNull)
        .map(MatchingCaseMapping::value)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(it -> !it.isEmpty())
        .map(it -> wrapper == NONE
                   ? it
                   : it.substring(1, it.length() - 1)
        )
        .map(it -> Stream.of(it.split("(,\\s*|\\s+)"))
            .map(String::trim)
            .toArray(String[]::new)
        )
        .filter(it -> it.length == 0)
        .orElseGet(() -> unwrap(method)
            .orElseGet(() -> Stream.of(method.getParameters())
                .map(it -> "*")
                .toArray(String[]::new)
            )
        );

    return Stream.of(arguments)
        .map(ControllerMethodDescriptor::createArgument)
        .toArray(Object[]::new);
  }

  private static Object createArgument (String str) {
    switch (str) {
    case "*":
    case "_":
    case "any":
      return any();
    case "anyBoolean":
      return anyBoolean();
    case "anyByte":
      return anyByte();
    case "anyChar":
      return anyChar();
    case "anyShort":
      return anyShort();
    case "anyInt":
      return anyInt();
    case "anyLong":
      return anyLong();
    case "anyFloat":
      return anyFloat();
    case "anyDouble":
      return anyDouble();
    case "anyString":
      return anyString();
    case "anyList":
      return anyList();
    case "anySet":
      return anySet();
    case "anyMap":
      return anyMap();
    case "anyCollection":
      return anyCollection();
    case "anyIterable":
      return anyIterable();
    default:
      break;
    }

    if (str.charAt(0) == ':') {
      return eq(unwrapString(str.substring(1)));
    }

    Integer intValue = getInteger(str);
    if (intValue != null) {
      return eq(intValue);
    }

    Double doubleValue = getDouble(str);
    if (doubleValue != null) {
      return eq(doubleValue);
    }

    return eq(unwrapString(str));
  }

  private static Integer getInteger (String string) {
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static Double getDouble (String string) {
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static String unwrapString (String string) {
    val firstChar = string.charAt(0);
    return firstChar == '\'' || firstChar == '"'
           ? string.substring(1, string.length() - 1)
           : string;
  }

  private static Optional<String[]> unwrap (Method method) {
    if (method.getParameterCount() != 1) {
      return empty();
    }

    Class<?> type = method.getParameterTypes()[0];
    if (!type.isAnnotationPresent(AsErlangList.class) &&
        !type.isAnnotationPresent(AsErlangTuple.class)) {
      return empty();
    }

    return Optional.of(type.getDeclaredFields())
        .map(fields -> Stream.of(fields)
            .filter(it -> !it.isSynthetic())
            .filter(it -> !it.isAnnotationPresent(IgnoreField.class))
            .map(it -> "*")
            .toArray(String[]::new)
        );
  }
}
