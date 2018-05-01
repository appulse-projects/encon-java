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

package io.appulse.encon.java.terms;

import static io.appulse.encon.java.terms.Erlang.NIL;
import static io.appulse.encon.java.terms.TermType.UNDEFINED;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import java.io.Serializable;

import io.appulse.encon.java.terms.exception.ErlangTermDecodeException;
import io.appulse.encon.java.terms.exception.IllegalErlangTermTypeException;
import io.appulse.encon.java.terms.term.BooleanTerm;
import io.appulse.encon.java.terms.term.CollectionTerm;
import io.appulse.encon.java.terms.term.FloatTerm;
import io.appulse.encon.java.terms.term.IntegerTerm;
import io.appulse.encon.java.terms.term.NodeContainerTerm;
import io.appulse.encon.java.terms.term.StringTerm;
import io.appulse.encon.java.terms.type.ErlangAtom;
import io.appulse.encon.java.terms.type.ErlangBinary;
import io.appulse.encon.java.terms.type.ErlangBitString;
import io.appulse.encon.java.terms.type.ErlangExternalFunction;
import io.appulse.encon.java.terms.type.ErlangFloat;
import io.appulse.encon.java.terms.type.ErlangFunction;
import io.appulse.encon.java.terms.type.ErlangInteger;
import io.appulse.encon.java.terms.type.ErlangList;
import io.appulse.encon.java.terms.type.ErlangMap;
import io.appulse.encon.java.terms.type.ErlangPid;
import io.appulse.encon.java.terms.type.ErlangPort;
import io.appulse.encon.java.terms.type.ErlangReference;
import io.appulse.encon.java.terms.type.ErlangString;
import io.appulse.encon.java.terms.type.ErlangTuple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@EqualsAndHashCode
@FieldDefaults(level = PRIVATE)
public abstract class ErlangTerm implements IntegerTerm,
                                            FloatTerm,
                                            BooleanTerm,
                                            StringTerm,
                                            CollectionTerm,
                                            NodeContainerTerm,
                                            Serializable {

  private static final long serialVersionUID = 5430854281567501819L;

  @SuppressWarnings("unchecked")
  public static <T extends ErlangTerm> T newInstance (@NonNull ByteBuf buffer) {
    val type = TermType.of(buffer.readByte());

    switch (type) {
    case SMALL_INTEGER:
    case INTEGER:
    case SMALL_BIG:
    case LARGE_BIG:
      return (T) new ErlangInteger(type, buffer);
    case FLOAT:
    case NEW_FLOAT:
      return (T) new ErlangFloat(type, buffer);
    case REFERENCE:
    case NEW_REFERENCE:
    case NEWER_REFERENCE:
      return (T) new ErlangReference(type, buffer);
    case PORT:
    case NEW_PORT:
      return (T) new ErlangPort(type, buffer);
    case PID:
    case NEW_PID:
      return (T) new ErlangPid(type, buffer);
    case SMALL_TUPLE:
    case LARGE_TUPLE:
      return (T) new ErlangTuple(type, buffer);
    case MAP:
      return (T) new ErlangMap(type, buffer);
    case NIL:
      return (T) NIL;
    case STRING:
      return (T) new ErlangString(type, buffer);
    case LIST:
      return (T) new ErlangList(type, buffer);
    case BINARY:
      return (T) new ErlangBinary(type, buffer);
    case FUNCTION:
    case NEW_FUNCTION:
      return (T) new ErlangFunction(type, buffer);
    case EXTERNAL_FUNCTION:
      return (T) new ErlangExternalFunction(type, buffer);
    case BIT_BINNARY:
      return (T) new ErlangBitString(type, buffer);
    case ATOM_UTF8:
    case SMALL_ATOM_UTF8:
    case ATOM:
    case SMALL_ATOM:
      return (T) new ErlangAtom(type, buffer);
    default:
      throw new ErlangTermDecodeException();
    }
  }

  @Setter(PROTECTED)
  TermType type;

  protected ErlangTerm () {
    type = UNDEFINED;
  }

  protected ErlangTerm (@NonNull TermType type) {
    if (!getClass().equals(type.getType())) {
      throw new IllegalErlangTermTypeException(getClass(), type);
    }
    this.type = type;
  }

  public final byte[] toBytes () {
    val buffer = Unpooled.buffer();
    writeTo(buffer);
    val bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    return bytes;
  }

  public final void writeTo (@NonNull ByteBuf buffer) {
    buffer.writeByte(type.getCode());
    serialize(buffer);
  }

  protected abstract void serialize (ByteBuf buffer);
}
