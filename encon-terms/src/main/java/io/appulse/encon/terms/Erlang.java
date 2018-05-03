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

package io.appulse.encon.terms;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;

import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.encon.terms.type.ErlangBinary;
import io.appulse.encon.terms.type.ErlangBitString;
import io.appulse.encon.terms.type.ErlangFloat;
import io.appulse.encon.terms.type.ErlangInteger;
import io.appulse.encon.terms.type.ErlangList;
import io.appulse.encon.terms.type.ErlangMap;
import io.appulse.encon.terms.type.ErlangNil;
import io.appulse.encon.terms.type.ErlangString;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.NonNull;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public final class Erlang {

  public static final ErlangNil NIL = new ErlangNil();

  public static final ErlangAtom EMPTY_ATOM = new ErlangAtom("");

  public static ErlangAtom atom (boolean value) {
    return new ErlangAtom(value);
  }

  public static ErlangAtom atom (@NonNull String value) {
    return new ErlangAtom(value);
  }

  public static ErlangInteger number (char value) {
    return new ErlangInteger(value);
  }

  public static ErlangInteger number (byte value) {
    return new ErlangInteger(value);
  }

  public static ErlangInteger number (short value) {
    return new ErlangInteger(value);
  }

  public static ErlangInteger number (int value) {
    return new ErlangInteger(value);
  }

  public static ErlangInteger number (long value) {
    return new ErlangInteger(value);
  }

  public static ErlangInteger number (@NonNull BigInteger value) {
    return new ErlangInteger(value);
  }

  public static ErlangFloat number (float value) {
    return new ErlangFloat(value);
  }

  public static ErlangFloat number (double value) {
    return new ErlangFloat(value);
  }

  public static ErlangBinary binary (@NonNull byte[] value) {
    return new ErlangBinary(value);
  }

  public static ErlangBitString bitstr (@NonNull byte[] bits, int pad) {
    return new ErlangBitString(bits, pad);
  }

  public static ErlangString string (@NonNull String value) {
    return new ErlangString(value); // List/BitString?
  }

  public static ErlangTuple tuple (@NonNull ErlangTerm... elements) {
    return new ErlangTuple(elements);
  }

  public static ErlangTuple tuple (@NonNull List<ErlangTerm> elements) {
    return new ErlangTuple(elements);
  }

  public static ErlangList list (@NonNull ErlangTerm... elements) {
    return new ErlangList(elements);
  }

  public static ErlangList list (@NonNull List<ErlangTerm> elements) {
    return new ErlangList(elements);
  }

  public static ErlangList list (@NonNull ErlangTerm tail, @NonNull List<ErlangTerm> elements) {
    return new ErlangList(tail, elements);
  }

  public static ErlangMap map (@NonNull ErlangTerm... keysAndValues) {
    if (keysAndValues.length % 2 != 0) {
      throw new IllegalArgumentException("Keys and Values array must be even");
    }

    LinkedHashMap<ErlangTerm, ErlangTerm> map = new LinkedHashMap<>(keysAndValues.length / 2);
    for (int index = 0; index < keysAndValues.length - 1; index += 2) {
      map.put(keysAndValues[index], keysAndValues[index + 1]);
    }
    return new ErlangMap(map);
  }

  private Erlang () {
  }
}
