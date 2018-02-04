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

import static io.appulse.encon.java.protocol.TermType.PORT;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.Builder;
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
public class Port extends ErlangTerm {

  String node;

  int id;

  int creation;

  public Port (TermType type) {
    super(type);
  }

  @Builder
  private Port (TermType type, @NonNull String node, int id, int creation) {
    this(ofNullable(type).orElse(PORT));
    this.node = node;

    switch (getType()) {
    case PORT:
      this.id = id & 0xFFFFFFF; // 28 bits
      this.creation = creation & 0x3; // 2 bits
      break;
    case NEW_PORT:
      this.id = id;
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

    switch (getType()) {
    case PORT:
      creation = buffer.getByte();
      break;
    case NEW_PORT:
      creation = buffer.getInt();
      break;
    default:
      throw new RuntimeException();
    }
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    // buffer.putTerm(new Atom(node));
    buffer.put4B(id);

    switch (getType()) {
    case PORT:
      buffer.put1B(creation);
      break;
    case NEW_PORT:
      buffer.put4B(creation);
      break;
    default:
      throw new RuntimeException();
    }
  }
}
