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

import static io.appulse.encon.java.protocol.TermType.LIST;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
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
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangList extends ErlangTerm {

  private static final long serialVersionUID = 6923705109793240922L;

  ErlangTerm[] elements;

  @Getter
  ErlangTerm tail;

  public ErlangList (TermType type) {
    super(type);
  }

  public ErlangList (ErlangTerm... elements) {
    this(Arrays.asList(elements));
  }

  public ErlangList (@NonNull List<ErlangTerm> elements) {
    this(LIST);

    this.elements = elements.toArray(new ErlangTerm[elements.size()]);
  }

  @Builder
  public ErlangList (ErlangTerm tail, @Singular List<ErlangTerm> elements) {
    this(LIST);
    this.elements = ofNullable(elements)
        .map(it -> it.toArray(new ErlangTerm[it.size()]))
        .orElse(new ErlangTerm[0]);

    this.tail = tail;
  }

  /**
   * Is proper list or not.
   *
   * @return is proper list or not
   */
  public boolean isProper () {
    return tail == null || tail.isNil();
  }

  @Override
  public boolean isTextual () {
    return isProper() && Stream.of(elements).allMatch(ErlangTerm::isIntegralNumber);
  }

  @Override
  public String asText (String defaultValue) {
    if (!isTextual()) {
      return defaultValue;
    }

    val codePoints = Stream.of(elements)
        .mapToInt(ErlangTerm::asInt)
        .toArray();

    return new String(codePoints, 0, codePoints.length);
  }

  @Override
  public ErlangList asList () {
    return this;
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
    val arity = buffer.getInt();
    elements = IntStream.range(0, arity)
        .boxed()
        .map(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);

    tail = ErlangTerm.newInstance(buffer);
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put4B(elements.length);
    Stream.of(elements)
        .map(ErlangTerm::toBytes)
        .forEachOrdered(buffer::put);

    if (tail == null) {
      tail = new ErlangNil();
    }
    buffer.put(tail.toBytes());
  }
}
