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

import static io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.NULL;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.Equals;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
@Slf4j
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class BuilderProxyMethodInterceptor implements MethodInterceptor {

  private static final MethodArgumentMatcher[] EMPTY = new MethodArgumentMatcher[0];

  List<MethodDescriptor> list;

  MethodArgumentsWrapper wrapper;

  Object target;

  @Override
  public Object intercept (Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    log.debug("method {}.{}({}), storage size: {}",
              obj.getClass().getSimpleName(), method.getName(), args, ThreadLocalStorage.size());

    MethodArgumentMatcher[] matchers = null;
    if (args == null || args.length == 0) {
      matchers = EMPTY;
    } else if (ThreadLocalStorage.isEmpty()) {
      matchers = Stream.of(args)
          .map(it -> it == null
                      ? NULL
                      : new Equals(it)
          )
          .toArray(MethodArgumentMatcher[]::new);
    } else if (ThreadLocalStorage.size() >= args.length) {
      matchers = ThreadLocalStorage.elements();
    } else {
      throw new IllegalArgumentException("Ambiguous method call");
    }
    ThreadLocalStorage.clear();

    MethodDescriptor descriptor = MethodDescriptor.builder()
        .proxy(target)
        .method(method)
        .matchers(matchers)
        .wrapper(wrapper)
        .build();

    list.add(descriptor);
    return null;
  }
}
