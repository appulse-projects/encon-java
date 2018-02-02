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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@SuppressFBWarnings("EI_EXPOSE_REP")
public final class Reference {

  String node;

  int[] ids;

  int creation;

  @Builder
  public Reference (@NonNull String node, int id, int[] ids, int creation) {
    this.node = node;
    this.ids = ids == null
               ? new int[] { id }
               : ids;
    this.creation = creation & 0x3; // 2 bits
  }
}
