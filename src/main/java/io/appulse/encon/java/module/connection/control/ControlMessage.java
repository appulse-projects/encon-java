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

package io.appulse.encon.java.protocol.control;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangInteger;
import io.appulse.encon.java.protocol.type.ErlangNil;
import io.appulse.encon.java.protocol.type.ErlangTuple;
import io.appulse.encon.java.protocol.type.ErlangTuple.ErlangTupleBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public abstract class ControlMessage {

  protected static final ErlangTerm UNUSED = new ErlangNil();

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static <T extends ControlMessage> T parse (@NonNull ErlangTerm term) {
    if (!term.isTuple()) {
      throw new RuntimeException();
    }
    val tuple = term.asTuple();

    val code = tuple.get(0)
        .filter(ErlangTerm::isNumber)
        .map(ErlangTerm::asInt)
        .orElseThrow(RuntimeException::new);

    val tag = Stream.of(ControlMessageTag.values())
        .filter(it -> it.getCode() == code)
        .findFirst()
        .orElseThrow(() -> new RuntimeException());

    Class<T> type = (Class<T>) tag.getType();
    Constructor<T> constructor = type.getConstructor(ErlangTuple.class);
    return constructor.newInstance(tuple);
  }

  public ErlangTuple toTuple () {
    ErlangTupleBuilder builder = ErlangTuple.builder();
    builder.add(ErlangInteger.from(getTag().getCode()));
    Stream.of(elements()).forEach(builder::add);
    return builder.build();
  }

  public byte[] toBytes () {
    return toTuple().toBytes();
  }

  public abstract ControlMessageTag getTag ();

  protected abstract ErlangTerm[] elements ();
}
