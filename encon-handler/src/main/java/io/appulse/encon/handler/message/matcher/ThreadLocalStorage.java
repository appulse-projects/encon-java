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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
final class ThreadLocalStorage {

  private static final ThreadLocal<List<MethodArgumentMatcher>> LIST = new ThreadLocal<List<MethodArgumentMatcher>>() {

    @Override
    protected List<MethodArgumentMatcher> initialValue () {
      return new LinkedList<>();
    }
  };

  static List<MethodArgumentMatcher> argumetMatchers () {
    return LIST.get();
  }

  static void add (MethodArgumentMatcher argumentMatcher) {
    LIST.get().add(argumentMatcher);
  }

  static MethodArgumentMatcher[] elements () {
    return LIST.get().toArray(new MethodArgumentMatcher[0]);
  }

  static boolean isEmpty () {
    return LIST.get().isEmpty();
  }

  static int size () {
    return LIST.get().size();
  }

  static void clear () {
    LIST.get().clear();
  }

  private ThreadLocalStorage () {
  }
}
