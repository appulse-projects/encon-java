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

import static io.appulse.encon.terms.TermType.LARGE_TUPLE;
import static io.appulse.encon.terms.TermType.SMALL_TUPLE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * A tuple is a compound data type with a fixed number of terms.
 * <p>
 * Each term Term in the tuple is called an element.
 * The number of elements is said to be the size of the tuple.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangTuple extends ErlangTerm {

  private static final long serialVersionUID = -8441946894531062971L;

  private static final int MAX_SMALL_TUPLE_SIZE = 256;

  ErlangTerm[] elements;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangTuple (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    int arity;
    switch (getType()) {
    case SMALL_TUPLE:
      arity = buffer.readByte();
      break;
    case LARGE_TUPLE:
      arity = buffer.readInt();
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }

    elements = IntStream.range(0, arity)
        .mapToObj(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);
  }

  /**
   * Constructs Erlang's tuple object with specific values.
   *
   * @param elements tuple's {@link ErlangTerm} elements
   */
  public ErlangTuple (ErlangTerm... elements) {
    this(Arrays.asList(elements));
  }

  /**
   * Constructs Erlang's tuple object with specific values.
   *
   * @param adds tuple's {@link ErlangTerm} elements
   */
  @Builder
  public ErlangTuple (@Singular Collection<ErlangTerm> adds) {
    super();

    if (adds.size() < MAX_SMALL_TUPLE_SIZE) {
      setType(SMALL_TUPLE);
    } else {
      setType(LARGE_TUPLE);
    }
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
  protected void serialize (ByteBuf buffer) {
    switch (getType()) {
    case SMALL_TUPLE:
      buffer.writeByte(elements.length);
      break;
    case LARGE_TUPLE:
      buffer.writeInt(elements.length);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
    Stream.of(elements)
        .forEachOrdered(it -> it.writeTo(buffer));
  }
}
