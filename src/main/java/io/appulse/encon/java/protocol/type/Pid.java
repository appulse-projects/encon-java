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

import static io.appulse.encon.java.protocol.TermType.PID;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class Pid extends ErlangTerm {

  String node;

  int id;

  int serial;

  int creation;

  public Pid (TermType type) {
    super(type);
  }

  @Builder
  private Pid (TermType type, @NonNull String node, int id, int serial, int creation) {
    this(ofNullable(type).orElse(PID));
    this.node = node;

    switch (getType()) {
    case PID:
      this.id = id & 0x7FFF; // 15 bits
      this.serial = serial & 0x1FFF; // 13 bits
      this.creation = creation & 0x03; // 2 bits
      break;
    case NEW_PID:
      // allow all 32 bits for NEW_PID
      this.id = id;
      this.serial = serial;
      this.creation = creation;
      break;
    default:
      throw new RuntimeException();
    }
  }

  @Override
  public String asText (String defaultValue) {
    return toString();
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    Atom atom = ErlangTerm.newInstance(buffer);
    node = atom.asText();

    id = buffer.getInt();
    serial = buffer.getInt();

    switch (getType()) {
    case PID:
      creation = buffer.getByte();
      break;
    case NEW_PID:
      creation = buffer.getInt();
      break;
    default:
      throw new RuntimeException();
    }
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put(new Atom(node).toBytes());
    buffer.put4B(id);
    buffer.put4B(serial);

    switch (getType()) {
    case PID:
      buffer.put1B(creation);
      break;
    case NEW_PID:
      buffer.put4B(creation);
      break;
    default:
      throw new RuntimeException();
    }
  }
}
