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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import io.appulse.encon.handler.message.exception.MethodInvocationException;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Method invoker.
 *
 * @since 1.6.0
 * @author alabazin
 */
@Slf4j
@Getter
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class MethodInvoker {

  @NonNull
  Object proxy;

  @NonNull
  Method method;

  @Getter(value = PRIVATE, lazy = true)
  String toString = createToString();

  Object invoke (Object... args) {
    if (log.isDebugEnabled()) {
      log.debug("invoking {}.{}({})", proxy.getClass().getSimpleName(), method.getName(), args);
    }
    try {
      return method.invoke(proxy, args);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      log.error("Exceptionally invoke method {}.{}({})",
                proxy.getClass().getSimpleName(), method.getName(), args, ex);

      throw new MethodInvocationException(proxy, method, args, ex);
    }
  }

  @Override
  public String toString () {
    return getToString();
  }

  private String createToString () {
    return new StringBuilder()
        .append(proxy.getClass().getSimpleName()).append('.')
        .append(method.getName()).append('(')
        .append(Stream.of(method.getParameterTypes())
            .map(Class::getSimpleName)
            .collect(joining(", ")))
        .append(')')
        .toString();
  }
}
