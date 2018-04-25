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

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.protocol.Erlang;
import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangPid extends ErlangTerm {

  private static final long serialVersionUID = 7083159089429831665L;

  NodeDescriptor descriptor;

  int id;

  int serial;

  int creation;

  public ErlangPid (TermType type) {
    super(type);
  }

  @Builder
  private ErlangPid (TermType type, @NonNull String node, int id, int serial, int creation) {
    this(ofNullable(type).orElse(PID));
    descriptor = NodeDescriptor.from(node);

    this.id = id;
    this.serial = serial;
    this.creation = creation;

    validate();
  }

  @Override
  public String asText (String defaultValue) {
    return toString();
  }

  @Override
  public ErlangPid asPid () {
    return this;
  }

  @Override
  public String toString () {
    return new StringBuilder()
        .append("#PID<")
        .append(creation).append('.')
        .append(id).append('.')
        .append(serial)
        .append('>')
        .toString();
  }

  @Override
  protected void read (ByteBuf buffer) {
    val atom = ErlangTerm.newInstance(buffer);
    descriptor = NodeDescriptor.from(atom.asText());

    id = buffer.readInt();
    serial = buffer.readInt();

    switch (getType()) {
    case PID:
      creation = buffer.readUnsignedByte();
      break;
    case NEW_PID:
      creation = buffer.readInt();
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }
    validate();
  }

  @Override
  protected void write (ByteBuf buffer) {
    Erlang.atom(descriptor.getFullName()).writeTo(buffer);
    buffer.writeInt(id);
    buffer.writeInt(serial);

    switch (getType()) {
    case PID:
      buffer.writeByte(creation);
      break;
    case NEW_PID:
      buffer.writeInt(creation);
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }
  }

  private void validate () {
    if (getType() == PID) {
      id = id & 0x7FFF; // 15 bits
      serial = serial & 0x1FFF; // 13 bits
      creation = creation & 0x03; // 2 bits;
    }
  }
}
