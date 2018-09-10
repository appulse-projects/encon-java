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

package io.appulse.encon.terms.term;

import static io.appulse.encon.terms.TermType.LARGE_TUPLE;
import static io.appulse.encon.terms.TermType.LIST;
import static io.appulse.encon.terms.TermType.MAP;
import static io.appulse.encon.terms.TermType.SMALL_TUPLE;
import static java.util.Collections.emptyIterator;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangList;
import io.appulse.encon.terms.type.ErlangMap;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.NonNull;
import lombok.val;

/**
 * Collection term API.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface CollectionTerm extends ValueTerm, Iterable<ErlangTerm> {

  /**
   * Tells is this term a collection or not.
   * <p>
   * Returns true if this term is one of:
   * <p>
   * <ul>
   * <li>{@link io.appulse.encon.terms.TermType#MAP}</li>
   * <li>{@link io.appulse.encon.terms.TermType#LIST}</li>
   * <li>{@link io.appulse.encon.terms.TermType#SMALL_TUPLE}</li>
   * <li>{@link io.appulse.encon.terms.TermType#LARGE_TUPLE}</li>
   * </ul>
   *
   * @return {@code true}, if this term is a list, tuple or map, {@code false} otherwise.
   */
  default boolean isCollectionTerm () {
    switch (getType()) {
    case MAP:
    case LIST:
    case SMALL_TUPLE:
    case LARGE_TUPLE:
      return true;
    default:
      return false;
    }
  }

  /**
   * Method that returns true if this term is a {@link io.appulse.encon.terms.TermType#LIST} term,
   * false otherwise.
   *
   * @return true, if this term is a list, false otherwise.
   */
  default boolean isList () {
    return getType() == LIST;
  }

  /**
   * Method that will try to convert value of this term to a {@link ErlangList} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangList} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangList} representation
   */
  default ErlangList asList () {
    return null;
  }

  /**
   * Method that returns true if this term is an {@link io.appulse.encon.terms.TermType#SMALL_TUPLE} or
   * {@link io.appulse.encon.terms.TermType#LARGE_TUPLE} term, false otherwise.
   *
   * @return true, if this term is a tuple, false otherwise.
   */
  default boolean isTuple () {
    return getType() == SMALL_TUPLE || getType() == LARGE_TUPLE;
  }

  /**
   * Method that will try to convert value of this term to a {@link ErlangTuple} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangTuple} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangTuple} representation
   */
  default ErlangTuple asTuple () {
    return null;
  }

  /**
   * Method that returns true if this term is a {@link io.appulse.encon.terms.TermType#MAP} term,
   * false otherwise.
   *
   * @return true, if this term is a map, false otherwise.
   */
  default boolean isMap () {
    return getType() == MAP;
  }

  /**
   * Method that will try to convert value of this term to a {@link ErlangMap} value.
   * <p>
   * If representation cannot be converted to a {@link ErlangMap} value,
   * {@code null} will be returned; no exceptions are thrown.
   *
   * @return term's {@link ErlangMap} representation
   */
  default ErlangMap asMap () {
    return null;
  }

  /**
   * Method that returns number of child terms this term contains:
   * for List/Tuple terms, number of child elements,
   * for Map term, number of fields,
   * and for all other terms 0.
   *
   * @return for non-container terms returns 0; for List/Tuple number of
   *         contained elements, and for Map number of fields.
   */
  default int size () {
    return 1;
  }

  /**
   * Method for accessing value of the specified element of an List, Tuple or Map term.
   * For other nodes, {@link Optional#empty} is always returned.
   * <p>
   * For List/Tuple terms, index specifies exact location within container and allows for efficient iteration
   * over child elements (underlying storage is guaranteed to be efficiently indexable, i.e. has random-access to
   * elements).
   * <p>
   * For Map terms, index specifies IntegralNumber field key.
   * <p>
   * If index is less than 0, or equal-or-greater than <code>term.size()</code>, {@link Optional#empty} is returned.
   * No exception is thrown for any index.
   *
   * @param index index in Container.
   *
   * @return Term that represent value of the specified element, if this term is an List, Tuple or Map and has
   *         specified element, {@link Optional#empty} otherwise.
   */
  default Optional<ErlangTerm> get (int index) {
    if (index < 0 || index >= size()) {
      return empty();
    }
    val result = getUnsafe(index);
    return ofNullable(result);
  }

  /**
   * Method for <b>unsafe</b> accessing value of the specified element of an List, Tuple or Map term.
   * For other nodes, {@code null} is always returned.
   * <p>
   * For List/Tuple terms, index specifies exact location within container and allows for efficient iteration
   * over child elements (underlying storage is guaranteed to be efficiently indexable, i.e. has random-access to
   * elements).
   * <p>
   * For Map terms, index specifies IntegralNumber field key.
   * <p>
   * If index is less than 0, or equal-or-greater than <code>term.size()</code>, {@code null} is returned.
   * No exception is thrown for any index.
   *
   * @param index index in this container.
   *
   * @return Term that represent value of the specified element, if this term is an List, Tuple or Map and has
   *         specified element, {@code null} otherwise.
   */
  default ErlangTerm getUnsafe (int index) {
    return index == 0
           ? (ErlangTerm) this
           : null;
  }

  /**
   * Method for accessing value of the specified field (as Atom) of an Map term.
   * If this term is not an Map (or it does not have a value for specified term), or
   * if there is no field with such term, {@link Optional#empty} is returned.
   *
   * @param fieldName key term (as Atom) in map.
   *
   * @return Term that represent value of the specified key term,
   *         if this term is an Map and has value for the specified
   *         key term. {@link Optional#empty} otherwise.
   */
  default Optional<ErlangTerm> getByAtom (@NonNull String fieldName) {
    val term = Erlang.atom(fieldName);
    return get(term);
  }

  /**
   * Method for accessing value of the specified field of an Map term.
   * If this term is not an Map (or it does not have a value for specified term), or
   * if there is no field with such term, {@link Optional#empty} is returned.
   *
   * @param term key term in map.
   *
   * @return Term that represent value of the specified key term,
   *         if this term is an Map and has value for the specified
   *         key term. {@link Optional#empty} otherwise.
   */
  default Optional<ErlangTerm> get (@NonNull ErlangTerm term) {
    val result = getUnsafe(term);
    return ofNullable(result);
  }

  /**
   * Method for <b>unsafe</b> accessing value of the specified field of an Map term.
   * If this term is not an Map (or it does not have a value for specified term), or
   * if there is no field with such term, {@code null} is returned.
   *
   * @param term key term in map.
   *
   * @return Term that represent value of the specified key term,
   *         if this term is an Map and has value for the specified
   *         key term. {@code null} otherwise.
   */
  default ErlangTerm getUnsafe (ErlangTerm term) {
    return null;
  }

  /**
   * Method that allows checking whether this term is Map term and contains a non null value for specified term as
   * key.
   * <p>
   * This method is equivalent to:
   * <pre>
   *   term.get(term).isPresent() &lt;&lt; !term.get(term).get().isNull()
   * </pre>
   *
   * @param term key term.
   *
   * @return true, if this term is a Map term and has a property entry with specified term,
   *         false otherwise.
   */
  default boolean hasNonNil (ErlangTerm term) {
    return get(term)
        .map(it -> !it.isNil())
        .orElse(false);
  }

  /**
   * Method that allows checking whether this term is Map term and contains a non null value for specified fieldName
   * as Atom.
   * <p>
   * This method is equivalent to:
   * <pre>
   *   term.getByAtom(fieldName).isPresent() &lt;&lt; !term.getByAtom(fieldName).get().isNull()
   * </pre>
   *
   * @param fieldName field name atom to check.
   *
   * @return true, if this term is a Map term and has a property entry with specified field name atom,
   *         false otherwise.
   */
  default boolean hasNonNilByAtom (String fieldName) {
    return getByAtom(fieldName)
        .map(it -> !it.isNil())
        .orElse(false);
  }

  /**
   * Method that allows checking whether this term is container term and contains a non null value for specified
   * index.
   * <p>
   * This method is equivalent to:
   * <pre>
   *   term.get(index).isPresent() &lt;&lt; !term.get(index).get().isNull()
   * </pre>
   *
   * @param index index to check.
   *
   * @return true, if this term is a container term and has a property entry with specified index,
   *         false otherwise.
   */
  default boolean hasNonNil (int index) {
    return get(index)
        .map(it -> !it.isNil())
        .orElse(false);
  }

  /**
   * Same as calling {@link #elements}, implemented so that convenience "for-each" loop
   * can be used for looping over elements.
   *
   * @return elements iterator.
   */
  @Override
  default Iterator<ErlangTerm> iterator () {
    return elements();
  }

  /**
   * Method for accessing all value terms of this Term, if
   * this term is a List, Tuple or Map term. In case of Map term,
   * field names (keys) are not included, only values.
   * For other types of terms, returns empty iterator.
   *
   * @return values iterator for Maps, Lists and Tuples.
   */
  default Iterator<ErlangTerm> elements () {
    return emptyIterator();
  }

  /**
   * Returns map's entries iterator that can be used to traverse all key/value pairs for map terms.
   * Empty iterator (no contents) for other types
   *
   * @return key-value map's entries iterator.
   */
  default Iterator<Map.Entry<ErlangTerm, ErlangTerm>> fields () {
    return emptyIterator();
  }

  /**
   * Method for accessing names of all fields for this term, if this node is an Map term.
   * Number of field names accessible will be {@link #size}.
   *
   * @return names iterator for Maps.
   */
  default Iterator<ErlangTerm> fieldNames () {
    return emptyIterator();
  }
}
