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
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static lombok.AccessLevel.PRIVATE;
import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class Tuple extends ErlangTerm {

  ErlangTerm[] elements;

  public Tuple (TermType type) {
    super(type);
  }

  public Tuple (ErlangTerm... elements) {
    this(asList(elements));
  }

  @Builder
  private Tuple (@Singular List<ErlangTerm> adds) {
    this(adds.size() < 256
         ? SMALL_TUPLE
         : LARGE_TUPLE);

    this.elements = adds.toArray(new ErlangTerm[adds.size()]);
  }

  @Override
  public Iterator<ErlangTerm> elements () {
    return Stream.of(elements).iterator();
  }

  @Override
  public Optional<ErlangTerm> get (int index) {
    return index >= 0 && index < size()
           ? of(elements[index])
           : empty();
  }

  @Override
  public int size () {
    return elements.length;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    int arity = 0;
    switch (getType()) {
    case SMALL_TUPLE:
      arity = buffer.getByte();
      break;
    case LARGE_TUPLE:
      arity = buffer.getInt();
      break;
    default:
      throw new RuntimeException();
    }

    elements = IntStream.range(0, arity)
        .boxed()
        .map(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    switch (getType()) {
    case SMALL_TUPLE:
      buffer.put1B(elements.length);
      break;
    case LARGE_TUPLE:
      buffer.put4B(elements.length);
      break;
    default:
      throw new RuntimeException();
    }

    Stream.of(elements)
        .map(ErlangTerm::toBytes)
        .forEachOrdered(buffer::put);
  }
}
