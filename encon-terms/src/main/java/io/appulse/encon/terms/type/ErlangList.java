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
import java.util.Collection;
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
 * A list is a compound data type with a variable number of terms.
 * <p>
 * Each term Term in the list is called an element.
 * The number of elements is said to be the length of the list.
 * <p>
 * Formally, a list is either the empty list {@code []} or consists of a <b>head</b> (first element)
 * and a <b>tail</b> (remainder of the list). The <b>tail</b> is also a list.
 * The latter can be expressed as {@code [H|T]}. The notation {@code [Term1,...,TermN]} above
 * is equivalent with the list {@code [Term1|[...|[TermN|[]]]]}.
 * <p>
 * Examples:
 * <p>
 * <ul>
 * <li>{@code []} is a list, thus</li>
 * <li>{@code [c|[]]} is a list, thus</li>
 * <li>{@code [b|[c|[]]]} is a list, thus</li>
 * <li>@{code [a|[b|[c|[]]]]} is a list, or in short {@code [a,b,c]}</li>
 * </ul>
 * <p>
 * A list where the tail is a list is sometimes called a <b>proper list</b>.
 * It is allowed to have a list where the tail is not a list, for example, {@code [a|b]}.
 * However, this type of list is of little practical use.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangList extends ErlangTerm {

  private static final long serialVersionUID = 6923705109793240922L;

  ErlangTerm[] elements;

  @Getter
  ErlangTerm tail;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangList (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val arity = buffer.readInt();
    elements = IntStream.range(0, arity)
        .boxed()
        .map(it -> ErlangTerm.newInstance(buffer))
        .toArray(ErlangTerm[]::new);

    tail = ErlangTerm.newInstance(buffer);
  }

  /**
   * Constructs Erlang's list object with specific values.
   *
   * @param elements list's {@link ErlangTerm} elements
   */
  public ErlangList (ErlangTerm... elements) {
    this(Arrays.asList(elements));
  }

  /**
   * Constructs Erlang's list object with specific values.
   *
   * @param elements list's {@link ErlangTerm} elements
   */
  public ErlangList (@NonNull Collection<ErlangTerm> elements) {
    super(LIST);

    this.elements = elements.toArray(new ErlangTerm[0]);
    tail = NIL;
  }

  /**
   * Constructs Erlang's list object with specific values and
   * optional (maybe {@code null}) {@link ErlangTerm} tail.
   *
   * @param tail     list's {@link ErlangTerm} tail
   *
   * @param elements list's {@link ErlangTerm} elements
   */
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
