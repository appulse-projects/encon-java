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

import static io.appulse.encon.java.protocol.TermType.NEW_REFERENCE;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.LongStream;

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
public class ErlangReference extends ErlangTerm {

  private static final long serialVersionUID = -868493639369161400L;

  private static final int MAX_REFERENCE_ARITY = 3;

  NodeDescriptor descriptor;

  long[] ids;

  int creation;

  public ErlangReference (TermType type) {
    super(type);
  }

  @Builder
  public ErlangReference (TermType type, @NonNull String node, long id, long[] ids, int creation) {
    this(ofNullable(type).orElse(NEW_REFERENCE));

    descriptor = NodeDescriptor.from(node);
    this.ids = ofNullable(ids)
        .map(it -> it.clone())
        .map(it -> {
          long[] result = new long[] { 0, 0, 0 };
          int length = it.length > 3
                       ? 3
                       : it.length;
          System.arraycopy(it, 0, result, 0, length);
          return result;
        })
        .orElseGet(() -> new long[] { id, 0, 0 });

    switch (getType()) {
    case NEW_REFERENCE:
      this.creation = creation & 0x3;
      this.ids[0] &= 0x3FFFF;
      break;
    case REFERENCE:
    case NEWER_REFERENCE:
      this.creation = creation;
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }
  }

  /**
   * Returns id.
   *
   * @return id
   */
  public long getId () {
    return ids[0];
  }

  @Override
  public String asText (String defaultValue) {
    return toString();
  }

  @Override
  public ErlangReference asReference () {
    return this;
  }

  @Override
  public String toString () {
    return new StringBuilder()
        .append("#Reference<")
        .append(creation).append('.')
        .append(ids[2]).append('.')
        .append(ids[1]).append('.')
        .append(ids[0])
        .append('>')
        .toString();
  }

  @Override
  protected void read (ByteBuf buffer) {
    switch (getType()) {
    case REFERENCE:
      ErlangAtom atom = ErlangTerm.newInstance(buffer);
      descriptor = NodeDescriptor.from(atom.asText());
      ids = new long[] { buffer.readInt() & 0x3FFFF };
      creation = buffer.readByte() & 0x03;
      return;
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }

    val arity = buffer.readShort();
    if (arity > MAX_REFERENCE_ARITY) {
      throw new IllegalArgumentException("Maximum arity value is " + MAX_REFERENCE_ARITY + ", but was " + arity);
    }

    ErlangAtom atom = ErlangTerm.newInstance(buffer);
    descriptor = NodeDescriptor.from(atom.asText());

    if (getType() == NEW_REFERENCE) {
      creation = buffer.readByte() & 0x3;
    } else {
      creation = buffer.readInt();
    }

    ids = LongStream.range(0, arity)
        .map(it -> buffer.readInt() & 0xFFFFFFFFL)
        .toArray();

    if (getType() == NEW_REFERENCE) {
      ids[0] &= 0x3FFFF;
    }
  }

  @Override
  protected void write (ByteBuf buffer) {
    switch (getType()) {
    case REFERENCE:
      Erlang.atom(descriptor.getFullName()).writeTo(buffer);
      buffer.writeInt((int) ids[0] & 0x3FFFF);
      buffer.writeByte(creation);
      return;
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }

    buffer.writeShort(ids.length);
    Erlang.atom(descriptor.getFullName()).writeTo(buffer);

    switch (getType()) {
    case NEW_REFERENCE:
      buffer.writeByte(creation);
      buffer.writeInt((int) ids[0] & 0x3FFFF);
      break;
    case NEWER_REFERENCE:
    default:
      buffer.writeInt(creation);
      buffer.writeInt((int) ids[0]);
    }

    LongStream.of(ids)
        .skip(1)
        .forEachOrdered(it -> buffer.writeInt((int) it));
  }
}
