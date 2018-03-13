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

import io.appulse.encon.java.NodeDescriptor;
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
 * @since 1.0.0
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangPort extends ErlangTerm {

  public static ErlangPort port (@NonNull String node, int id, int creation) {
    return new ErlangPort(PORT, node, id, creation);
  }

  NodeDescriptor descriptor;

  int id;

  int creation;

  public ErlangPort (TermType type) {
    super(type);
  }

  @Builder
  private ErlangPort (TermType type, @NonNull String node, int id, int creation) {
    this(ofNullable(type).orElse(PORT));
    descriptor = NodeDescriptor.from(node);

    this.id = id;
    this.creation = creation;

    validate();
  }

  @Override
  public String asText (String defaultValue) {
    return toString();
  }

  @Override
  public ErlangPort asPort () {
    return this;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    ErlangAtom atom = ErlangTerm.newInstance(buffer);
    descriptor = NodeDescriptor.from(atom.asText());
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
    validate();
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put(new ErlangAtom(descriptor.getFullName()).toBytes());
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

  private void validate () {
    if (getType() == PORT) {
      id = id & 0xFFFFFFF; // 28 bits
      creation = creation & 0x3; // 2 bits
    }
  }
}
