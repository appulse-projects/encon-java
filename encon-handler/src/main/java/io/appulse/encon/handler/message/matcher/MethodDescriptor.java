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

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

import io.appulse.encon.databind.annotation.IgnoreField;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.InstanceOf;
import io.appulse.encon.terms.ErlangTerm;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Slf4j
@Getter
@ToString
@FieldDefaults(level = PRIVATE, makeFinal = true)
final class MethodDescriptor {

  private static boolean isUserPojo (Class<?> type) {
    return ofNullable(type)
        .map(Class::getPackage)
        .filter(Objects::nonNull)
        .map(Package::getName)
        .filter(Objects::nonNull)
        .filter(it -> !it.startsWith("java."))
        .filter(it -> !it.startsWith("io.appulse.encon.terms."))
        .isPresent();
  }

  MethodMatcher matcher;

  MethodArgumentsTransformer transformer;

  MethodInvoker invoker;

  @Builder
  private MethodDescriptor (@NonNull Object proxy,
                            @NonNull Method method,
                            @NonNull MethodArgumentsWrapper wrapper,
                            MethodArgumentMatcher[] matchers
  ) {
    Class<?>[] types = method.getParameterTypes();
    Class<?> userPojoType = null;

    if (types.length == 1 && isUserPojo(types[0])) {
      userPojoType = types[0];
      types = Stream.of(userPojoType.getDeclaredFields())
          .filter(it -> !it.isAnnotationPresent(IgnoreField.class))
          .map(Field::getType)
          .toArray(Class<?>[]::new);
    }

    MethodArgumentMatcher[] argumentMatchers = ofNullable(matchers)
        .orElse(Stream.of(types)
            .map(it -> new InstanceOf(it))
            .toArray(MethodArgumentMatcher[]::new)
        );

    matcher = new MethodMatcher(wrapper, argumentMatchers);
    transformer = new MethodArgumentsTransformer(userPojoType, types);
    invoker = new MethodInvoker(proxy, method);
  }

  int elements () {
    return matcher.getMatchers().length;
  }

  boolean matches (ErlangTerm term) {
    return matcher.matches(term);
  }

  Object invoke (ErlangTerm term) {
    Object[] arguments = transformer.transform(term);
    log.debug("transforming term {} into {}", term, arguments);
    return invoker.invoke(arguments);
  }
}
