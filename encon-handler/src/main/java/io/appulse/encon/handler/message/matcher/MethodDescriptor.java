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

import static io.appulse.encon.databind.TermMapper.deserialize;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.terms.ErlangTerm;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
@Slf4j
@Value
@Builder
class MethodDescriptor {

  Object proxy;

  Method method;

  MethodArgumentsWrapper wrapper;

  MethodArgumentMatcher[] matchers;

  @Override
  public String toString () {
    val str = Stream.of(matchers)
        .map(Object::getClass)
        .map(Class::getSimpleName)
        .collect(joining(","));

    return new StringBuilder()
        .append("MethodDescriptor(")
        .append("proxy=").append(proxy.getClass().getSimpleName()).append(", ")
        .append("method=").append(method.getName()).append(", ")
        .append("wrapper=").append(wrapper).append(", ")
        .append("matchers=").append(str).append(')')
        .toString();
  }

  @SneakyThrows
  void invoke (ErlangTerm term) {
    if (!term.isCollectionTerm()) {
      Class<?> type = method.getParameterTypes()[0];
      Object argument = deserialize(term, type);
      method.invoke(proxy, argument);
      return;
    }

    Class<?>[] types = method.getParameterTypes();
    Object[] arguments = IntStream.range(0, types.length)
        .mapToObj(index -> deserialize(term.getUnsafe(index), types[index]))
        .toArray();

    method.invoke(proxy, arguments);
  }

  boolean isApplicable (ErlangTerm term) {
    if (!wrapper.isApplicable(term) || matchers.length != term.size()) {
      return false;
    }

    if (!term.isCollectionTerm()) {
      return matchers.length == 1 &&
             matchers[0].matches(term);
    }

    for (int index = 0; index < matchers.length; index++) {
      val element = term.getUnsafe(index);
      val matcher = matchers[index];
      if (!matcher.matches(element)) {
        return false;
      }
    }
    return true;
  }
}
