/*
 * Copyright 2018 the original author or authors..
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
package io.appulse.encon.handler.mock;

import java.lang.reflect.Method;

import io.appulse.encon.terms.ErlangTerm;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 *
 * @author alabazin
 */
@Value
@Builder
class MethodDescriptor {

  Object proxy;

  Method method;

  ArgumentMatcher[] matchers;

  ArgumentsWrapper wrapper;

  @SneakyThrows
  void invoke (Object... arguments) {
    method.invoke(proxy, arguments);
  }

  boolean isApplicable (ErlangTerm term) {
    if (!wrapper.isApplicable(term)) {
      return false;
    }

    if (!term.isCollectionTerm()) {
      return matchers.length == 1 &&
             matchers[0].matches(term);
    } else if (matchers.length != term.size()) {
      return false;
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
