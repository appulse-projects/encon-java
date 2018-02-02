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

package io.appulse.encon.java;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.encon.java.protocol.type.Pid;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class GeneratorPid {

  String nodeName;

  int creation;

  AtomicInteger count = new AtomicInteger(0);

  AtomicInteger serial = new AtomicInteger(0);

  public Pid generate () {
    int nextId = nextId();
    int nextSerial = nextSerial(nextId);

    return Pid.builder()
        .node(nodeName)
        .id(nextId)
        .serial(nextSerial)
        .creation(creation)
        .build();
  }

  private int nextId () {
    int current;
    int next;
    do {
      current = count.get();
      next = (current + 1) % 0x7FFF;
    } while (!count.compareAndSet(current, next));
    return next;
  }

  private int nextSerial (int id) {
    if (id != 0) {
      return serial.get();
    }

    int current;
    int next;
    do {
      current = serial.get();
      next = (current + 1) % 0x1FFF; // 13 bits
    } while (!serial.compareAndSet(current, next));
    return next;
  }
}
