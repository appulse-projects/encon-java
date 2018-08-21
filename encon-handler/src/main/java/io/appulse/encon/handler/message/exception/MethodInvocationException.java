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

package io.appulse.encon.handler.message.exception;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.6.0
 * @author alabazin
 */
@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MethodInvocationException extends RuntimeException {

  private static final long serialVersionUID = -4515267318144568448L;

  Object object;

  Method method;

  Object[] args;

  public MethodInvocationException (@NonNull Object object, @NonNull Method method, Object[] args, Throwable throwable) {
    super(throwable);
    this.object = object;
    this.method = method;
    this.args = ofNullable(args).orElse(new Object[0]);
  }

  @Override
  public String getMessage () {
    return new StringBuilder()
        .append(object.getClass().getSimpleName()).append('.')
        .append(method.getName()).append('(')
        .append(ofNullable(args)
            .map(it -> Stream.of(it)
                .map(Object::toString)
                .collect(joining(", "))
            )
            .orElse("")
        )
        .append(')')
        .toString();
  }
}
