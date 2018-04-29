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

import static io.appulse.encon.java.protocol.TermType.LARGE_TUPLE;
import static io.appulse.encon.java.protocol.TermType.SMALL_TUPLE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangTuple extends ErlangTerm {

  private static final long serialVersionUID = -8441946894531062971L;

  ErlangTerm[] elements;

  public ErlangTuple (TermType type) {
    super(type);
  }

  public ErlangTuple (ErlangTerm... elements) {
    this(Arrays.asList(elements));
  }

  @Builder
  public ErlangTuple (@Singular List<ErlangTerm> adds) {
    this(adds.size() < 256
         ? SMALL_TUPLE
         : LARGE_TUPLE);

    this.elements = adds.toArray(new ErlangTerm[0]);
  }

  @Override
  public ErlangTuple asTuple () {
    return this;
  }

  @Override
  public Iterator<ErlangTerm> elements () {
    return Stream.of(elements).iterator();
  }

  @Override
  public ErlangTerm getUnsafe (int index) {
    return elements[index];
  }

  @Override
  public int size () {
    return elements.length;
  }

  @Override
  protected void read (ByteBuf buffer) {
    int arity;
    switch (getType()) {
    case SMALL_TUPLE:
      arity = buffer.readByte();
      break;
    case LARGE_TUPLE:
      arity = buffer.readInt();
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }

    elements = IntStream.range(0, arity)
        .mapToObj(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);
  }

  @Override
  protected void write (ByteBuf buffer) {
    switch (getType()) {
    case SMALL_TUPLE:
      buffer.writeByte(elements.length);
      break;
    case LARGE_TUPLE:
      buffer.writeInt(elements.length);
      break;
    default:
      throw new IllegalArgumentException("Unknown type: " + getType());
    }
    Stream.of(elements)
        .forEachOrdered(it -> it.writeTo(buffer));
  }
}
