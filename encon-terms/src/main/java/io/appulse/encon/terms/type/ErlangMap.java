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

import static io.appulse.encon.terms.TermType.MAP;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.ErlangTermDecodeException;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * A map is a compound data type with a variable number of key-value associations.
 * <p>
 * Each key-value association in the map is called an <b>association pair</b>.
 * The key and value parts of the pair are called <b>elements</b>.
 * The number of association pairs is said to be the <b>size</b> of the map.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.LooseCoupling") // we realy need to use LinkedHashMap
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangMap extends ErlangTerm {

  private static final long serialVersionUID = -4889715199209923662L;

  LinkedHashMap<? extends ErlangTerm, ? extends ErlangTerm> map;

  /**
   * Constructs Erlang term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangMap (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    IntFunction<ErlangTerm[]> mapFunction = it -> new ErlangTerm[] {
        ErlangTerm.newInstance(buffer),
        ErlangTerm.newInstance(buffer)
    };

    BinaryOperator<ErlangTerm> mergeFunction = (left, right) -> {
      throw new ErlangTermDecodeException("Duplicate key " + left);
    };

    val arity = buffer.readInt();
    map = IntStream.range(0, arity)
        .mapToObj(mapFunction)
        .collect(toMap(it -> it[0], it -> it[1], mergeFunction, LinkedHashMap::new));
  }

  /**
   * Constructs Erlang's map object with specific {@link LinkedHashMap} values.
   *
   * @param map its {@link LinkedHashMap} values
   */
  public ErlangMap (@NonNull LinkedHashMap<? extends ErlangTerm, ? extends ErlangTerm> map) {
    super(MAP);
    this.map = map;
  }

  @Override
  public ErlangMap asMap () {
    return this;
  }

  @Override
  public Iterator<ErlangTerm> elements () {
    return map.values()
        .stream()
        .map(it -> (ErlangTerm) it)
        .iterator();
  }

  @Override
  public Iterator<Entry<ErlangTerm, ErlangTerm>> fields () {
    return map.entrySet()
        .stream()
        .map(it -> new SimpleEntry<>(
            it.getKey(),
            it.getValue()
        ))
        .map(it -> (Entry<ErlangTerm, ErlangTerm>) it)
        .iterator();
  }

  @Override
  public Iterator<ErlangTerm> fieldNames () {
    return map.keySet()
        .stream()
        .map(it -> (ErlangTerm) it)
        .iterator();
  }

  @Override
  public ErlangTerm getUnsafe (ErlangTerm term) {
    return map.get(term);
  }

  @Override
  public ErlangTerm getUnsafe (int index) {
    val term = new ErlangInteger(index);
    return map.get(term);
  }

  @Override
  public int size () {
    return map.size();
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    buffer.writeInt(map.size());
    map.entrySet().forEach(it -> {
      it.getKey().writeTo(buffer);
      it.getValue().writeTo(buffer);
    });
  }
}
