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

import static io.appulse.encon.terms.Erlang.NIL;
import static io.appulse.encon.terms.TermType.LIST;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;

import io.netty.buffer.ByteBuf;
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
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangList extends ErlangTerm {

  private static final long serialVersionUID = 6923705109793240922L;

  ErlangTerm[] elements;

  @Getter
  ErlangTerm tail;

  public ErlangList (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val arity = buffer.readInt();
    elements = IntStream.range(0, arity)
        .boxed()
        .map(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);

    tail = ErlangTerm.newInstance(buffer);
  }

  public ErlangList (ErlangTerm... elements) {
    this(Arrays.asList(elements));
  }

  public ErlangList (@NonNull List<ErlangTerm> elements) {
    super(LIST);

    this.elements = elements.toArray(new ErlangTerm[0]);
    tail = NIL;
  }

  @Builder
  public ErlangList (ErlangTerm tail, @Singular List<ErlangTerm> elements) {
    super(LIST);
    this.elements = ofNullable(elements)
        .map(it -> it.toArray(new ErlangTerm[0]))
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
  public ErlangTerm getUnsafe (int index) {
    return elements[index];
  }

  @Override
  public int size () {
    return elements.length;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    buffer.writeInt(elements.length);
    Stream.of(elements)
        .forEachOrdered(it -> it.writeTo(buffer));

    tail.writeTo(buffer);
  }
}
