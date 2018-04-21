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

package io.appulse.encon.java.module.generator.reference;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.atomic.AtomicInteger;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.type.ErlangReference;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class ReferenceGeneratorModule implements ReferenceGeneratorModuleApi {

  NodeInternalApi internal;

  AtomicInteger id1 = new AtomicInteger(0);

  AtomicInteger id2 = new AtomicInteger(0);

  AtomicInteger id3 = new AtomicInteger(0);

  @Override
  public ErlangReference generateReference () {
    val nextId1 = nextId1();

    val nextId2 = nextId1 == 0
                ? id2.incrementAndGet()
                : id2.get();

    val nextId3 = nextId1 == 0 && nextId2 == 0
                ? id3.incrementAndGet()
                : id3.get();

    return ErlangReference.builder()
        .node(internal.node().getDescriptor().getFullName())
        .ids(new long[] { nextId1, nextId2, nextId3 })
        .creation(internal.creation())
        .build();
  }

  private int nextId1 () {
    int current;
    int next;
    do {
      current = id1.get();
      next = (current + 1) % 0x3FFFF;
    } while (!id1.compareAndSet(current, next));
    return next;
  }
}
