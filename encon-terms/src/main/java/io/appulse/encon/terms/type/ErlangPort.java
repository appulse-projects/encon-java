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

import static io.appulse.encon.terms.TermType.PORT;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

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
 * A port identifier identifies an Erlang port.
 * <p>
 * Ports provide the basic mechanism for communication with the external world,
 * from Erlang's point of view. They provide a byte-oriented interface to an external program.
 * When a port has been created, Erlang can communicate with it by sending and receiving lists of bytes,
 * including binaries.
 * <p>
 * The Erlang process creating a port is said to be the <b>port owner</b>, or
 * the <b>connected process</b> of the port. All communication to and from the port must go
 * through the port owner. If the port owner terminates, so does the port (and the external program,
 * if it is written correctly).
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangPort extends ErlangTerm {

  private static final long serialVersionUID = 6837541013041204637L;

  @NonFinal
  NodeDescriptor descriptor;

  ErlangAtom node;

  int id;

  int creation;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangPort (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    node = ErlangTerm.newInstance(buffer);

    switch (getType()) {
    case PORT:
      id = buffer.readInt() & 0xFFFFFFF; // 28 bits
      creation = buffer.readUnsignedByte() & 0x3; // 2 bits
      break;
    case NEW_PORT:
      id = buffer.readInt();
      creation = buffer.readInt();
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }

  @Builder
  private ErlangPort (TermType type, @NonNull String node, int id, int creation) {
    super(ofNullable(type).orElse(PORT));
    this.node = Erlang.atom(node);

    if (getType() == PORT) {
      this.id = id & 0xFFFFFFF; // 28 bits
      this.creation = creation & 0x3; // 2 bits
    } else {
      this.id = id;
      this.creation = creation;
    }
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
  public String toString () {
    return new StringBuilder()
        .append("#Port<")
        .append(creation).append('.')
        .append(id)
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

    switch (getType()) {
    case PORT:
      buffer.writeByte(creation);
      break;
    case NEW_PORT:
      buffer.writeInt(creation);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }
}
