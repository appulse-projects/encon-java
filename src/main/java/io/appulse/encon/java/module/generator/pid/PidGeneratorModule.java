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

package io.appulse.encon.java.module.generator.pid;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.type.ErlangPid;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class PidGeneratorModule implements PidGeneratorModuleApi {

  NodeInternalApi internal;

  AtomicInteger count = new AtomicInteger(0);

  AtomicInteger serial = new AtomicInteger(0);

  @Override
  public ErlangPid generatePid () {
    int nextId = nextId();
    int nextSerial = nextSerial(nextId);

    return ErlangPid.builder()
        .node(internal.node().getDescriptor().getFullName())
        .id(nextId)
        .serial(nextSerial)
        .creation(internal.creation())
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

