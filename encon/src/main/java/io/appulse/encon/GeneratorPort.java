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

package io.appulse.encon;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.encon.terms.type.ErlangPort;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class GeneratorPort {

  String fullName;

  int creation;

  AtomicInteger count = new AtomicInteger(0);

  ErlangPort generate () {
    return ErlangPort.builder()
        .node(fullName)
        .id(nextId())
        .creation(creation)
        .build();
  }

  private int nextId () {
    int current;
    int next;
    do {
      current = count.get();
      next = (current + 1) % 0xFFFFFFF; // 28 bits
    } while (!count.compareAndSet(current, next));
    return next;
  }
}
