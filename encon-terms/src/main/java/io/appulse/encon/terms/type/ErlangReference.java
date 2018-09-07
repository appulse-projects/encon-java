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

package io.appulse.encon.terms.type;

import static io.appulse.encon.terms.TermType.NEW_REFERENCE;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.stream.LongStream;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * A reference is a term that is unique in an Erlang runtime system.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
public class ErlangReference extends ErlangTerm {

  private static final long serialVersionUID = -868493639369161400L;

  private static final int MAX_REFERENCE_ARITY = 3;

  @NonFinal
  NodeDescriptor descriptor;

  ErlangAtom node;

  long[] ids;

  int creation;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangReference (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    switch (getType()) {
    case REFERENCE:
      node = ErlangTerm.newInstance(buffer);
      ids = new long[] { buffer.readInt() & 0x3FFFF };
      creation = buffer.readByte() & 0x03;
      return;
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }

    int arity = buffer.readShort();
    if (arity > MAX_REFERENCE_ARITY) {
      throw new IllegalArgumentException("Maximum arity value is " + MAX_REFERENCE_ARITY + ", but was " + arity);
    }

    node = ErlangTerm.newInstance(buffer);

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

  @Builder
  private ErlangReference (TermType type, @NonNull String node, long id, long[] ids, int creation) {
    super(ofNullable(type).orElse(NEW_REFERENCE));

    this.node = Erlang.atom(node);
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
        .orElseGet(() -> new long[] { id });

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
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }

  /**
   * Returns single id element.
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

  /**
   * Returns container's {@link NodeDescriptor} value.
   *
   * @return container's {@link NodeDescriptor} value
   */
  public final NodeDescriptor getDescriptor () {
    if (descriptor == null) {
      descriptor = NodeDescriptor.from(node.asText());
    }
    return descriptor;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    switch (getType()) {
    case REFERENCE:
      node.writeTo(buffer);
      buffer.writeInt((int) ids[0] & 0x3FFFF);
      buffer.writeByte(creation);
      return;
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }

    buffer.writeShort(ids.length);
    node.writeTo(buffer);

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
