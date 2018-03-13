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

import static io.appulse.encon.java.protocol.TermType.MAP;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
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
public class ErlangMap extends ErlangTerm {

  private static final long serialVersionUID = -4889715199209923662L;

  public static ErlangMap map (@NonNull ErlangTerm... keysAndValues) {
    if (keysAndValues.length % 2 != 0) {
      throw new IllegalArgumentException();
    }

    LinkedHashMap<ErlangTerm, ErlangTerm> map = new LinkedHashMap<>(keysAndValues.length / 2);
    for (int index = 0; index < keysAndValues.length - 1; index += 2) {
      map.put(keysAndValues[index], keysAndValues[index + 1]);
    }
    return new ErlangMap(map);
  }

  LinkedHashMap<ErlangTerm, ErlangTerm> map;

  public ErlangMap (TermType type) {
    super(type);
  }

  public ErlangMap (@NonNull LinkedHashMap<ErlangTerm, ErlangTerm> map) {
    this(MAP);
    this.map = map;
  }

  @Override
  public ErlangMap asMap () {
    return this;
  }

  @Override
  public Iterator<ErlangTerm> elements () {
    return map.values().iterator();
  }

  @Override
  public Iterator<Map.Entry<ErlangTerm, ErlangTerm>> fields () {
    return map.entrySet().iterator();
  }

  @Override
  public Iterator<ErlangTerm> fieldNames () {
    return map.keySet().iterator();
  }

  @Override
  public Optional<ErlangTerm> get (ErlangTerm term) {
    return ofNullable(map.get(term));
  }

  @Override
  public Optional<ErlangTerm> get (int index) {
    return get(new ErlangInteger(index));
  }

  @Override
  public Optional<ErlangTerm> getByAtom (String fieldName) {
    return get(new ErlangAtom(fieldName));
  }

  @Override
  public int size () {
    return map.size();
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    IntFunction<ErlangTerm[]> mapFunction = it -> new ErlangTerm[] {
        ErlangTerm.newInstance(buffer),
        ErlangTerm.newInstance(buffer)
    };

    BinaryOperator<ErlangTerm> mergeFunction = (left, right) -> {
      throw new IllegalStateException("Duplicate key " + left);
    };

    val arity = buffer.getInt();
    map = IntStream.range(0, arity)
        .mapToObj(mapFunction)
        .collect(toMap(it -> it[0], it -> it[1], mergeFunction, LinkedHashMap::new));
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put4B(map.size());
    map.entrySet().forEach(it -> {
      buffer.put(it.getKey().toBytes());
      buffer.put(it.getValue().toBytes());
    });
  }
}
