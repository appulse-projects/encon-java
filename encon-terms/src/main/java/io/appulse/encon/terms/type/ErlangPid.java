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

import static io.appulse.encon.terms.TermType.PID;
import static io.appulse.encon.terms.TermType.SMALL_ATOM;
import static io.appulse.encon.terms.TermType.SMALL_ATOM_UTF8;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.common.LruCache;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * A process identifier, pid, identifies a process.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangPid extends ErlangTerm {

  private static final long serialVersionUID = 7083159089429831665L;

  private static final LruCache<Integer, ErlangPid> CACHE = new LruCache<>(1000);

  @SuppressWarnings("deprecation")
  public static ErlangPid cached (TermType type, ByteBuf buffer) {
    int index = buffer.readerIndex();
    byte nodeNameType = buffer.readByte();
    int nodeNameLength = nodeNameType == SMALL_ATOM.getCode() || nodeNameType == SMALL_ATOM_UTF8.getCode()
                         ? buffer.readUnsignedByte()
                         : buffer.readUnsignedShort();

    int length = type == PID
                 ? nodeNameLength + 9
                 : nodeNameLength + 12;

    ByteArrayHashCode byteProcessor = new ByteArrayHashCode();
    buffer.forEachByte(buffer.readerIndex(), length, byteProcessor);

    return CACHE.compute(byteProcessor.getHashCode(), (key, value) -> {
      if (value == null) {
        buffer.readerIndex(index);
        return new ErlangPid(type, buffer);
      } else {
        buffer.skipBytes(length);
        return value;
      }
    });
  }

  @NonFinal
  NodeDescriptor descriptor;

  ErlangAtom node;

  int id;

  int serial;

  int creation;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangPid (TermType type, @NonNull ByteBuf buffer) {
    super(type);
    node = ErlangTerm.newInstance(buffer);

    switch (type) {
    case PID:
      this.id = buffer.readInt() & 0x7FFF; // 15 bits
      this.serial = buffer.readInt() & 0x1FFF; // 13 bits
      this.creation = buffer.readUnsignedByte() & 0x03; // 2 bits;
      break;
    case NEW_PID:
      this.id = buffer.readInt();
      this.serial = buffer.readInt();
      this.creation = buffer.readInt();
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), type);
    }
  }

  @Builder
  private ErlangPid (TermType type, @NonNull String node, int id, int serial, int creation) {
    super(ofNullable(type).orElse(PID));
    this.node = Erlang.atom(node);

    if (getType() == PID) {
      this.id = id & 0x7FFF; // 15 bits
      this.serial = serial & 0x1FFF; // 13 bits
      this.creation = creation & 0x03; // 2 bits;
    } else {
      this.id = id;
      this.serial = serial;
      this.creation = creation;
    }
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
    node.writeTo(buffer);
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
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }

  @Getter
  @FieldDefaults(level = PRIVATE)
  private static class ByteArrayHashCode implements ByteProcessor {

    int hashCode = 1;

    @Override
    public boolean process (byte value) throws Exception {
      hashCode = 31 * hashCode + value;
      return true;
    }
  }
}
