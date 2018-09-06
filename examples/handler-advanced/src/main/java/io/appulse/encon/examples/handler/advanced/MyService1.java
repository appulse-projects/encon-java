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

package io.appulse.encon.examples.handler.advanced;

import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedList;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class MyService1 {

  LinkedList<String> callHistory;

  public void handler1 () {
    callHistory.add("handler1()");
  }

  public void handler2 (int a, String b, boolean c) {
    callHistory.add("handler2(" + a + ", " + b + ", " + c + ")");
  }
}
