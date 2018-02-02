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

package io.appulse.encon.java.protocol.type;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = PRIVATE)
public final class Pid {

  String node;

  int id;

  int serial;

  int creation;

  @Builder
  public Pid (@NonNull String node, int id, int serial, int creation) {
    this.node = node;
    this.id = id & 0x7FFF; // 15 bits
    this.serial = serial & 0x1FFF; // 13 bits
    this.creation = creation & 0x03; // 2 bits
  }
}
