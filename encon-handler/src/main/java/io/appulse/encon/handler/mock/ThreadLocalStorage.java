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

import java.util.Stack;

/**
 *
 * @author alabazin
 */
public final class ThreadLocalStorage {

  private static final ThreadLocal<Stack<ArgumentMatcher<?>>> STACK = new ThreadLocal<Stack<ArgumentMatcher<?>>>() {

    @Override
    protected Stack<ArgumentMatcher<?>> initialValue() {
      return new Stack<>();
    }
  };

  /**
   * Returns the {@link MockingProgress} for the current Thread.
   * <p>
   * <b>IMPORTANT</b>: Never assign and access the returned {@link MockingProgress} to an instance or static field. Thread safety can not be guaranteed in this case, cause the Thread that wrote the field might not be the same that read it. In other words multiple threads will access the same {@link MockingProgress}.
   *
   * @return never <code>null</code>
   */
  public static Stack<ArgumentMatcher<?>> stack() {
    return STACK.get();
  }

  private ThreadLocalStorage() {
  }
}
