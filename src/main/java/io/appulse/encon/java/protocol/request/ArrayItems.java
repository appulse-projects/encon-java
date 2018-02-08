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

package io.appulse.encon.java.protocol.request;

import static lombok.AccessLevel.PRIVATE;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Atom;
import io.appulse.encon.java.protocol.type.Binary;
import io.appulse.encon.java.protocol.type.ErlangList;
import io.appulse.encon.java.protocol.type.ErlangString;
import io.appulse.encon.java.protocol.type.FloatingPointNumber;
import io.appulse.encon.java.protocol.type.IntegralNumber;
import io.appulse.encon.java.protocol.type.Nil;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.encon.java.protocol.type.Port;
import io.appulse.encon.java.protocol.type.Reference;
import io.appulse.encon.java.protocol.type.Tuple;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ArrayItems {

  public static ArrayItems items () {
    return new ArrayItems();
  }

  List<ErlangTerm> terms = new LinkedList<>();

  public ArrayItems add (boolean value) {
    terms.add(new Atom(value));
    return this;
  }

  public ArrayItems add (char value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (byte value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (@NonNull byte[] value) {
    terms.add(new Binary(value));
    return this;
  }

  public ArrayItems add (short value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (int value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (long value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (@NonNull BigInteger value) {
    terms.add(new IntegralNumber(value));
    return this;
  }

  public ArrayItems add (float value) {
    terms.add(new FloatingPointNumber(value));
    return this;
  }

  public ArrayItems add (double value) {
    terms.add(new FloatingPointNumber(value));
    return this;
  }

  public ArrayItems addAtom (@NonNull String value) {
    terms.add(new Atom(value));
    return this;
  }

  public ArrayItems addNil () {
    terms.add(new Nil());
    return this;
  }

  public ArrayItems add (@NonNull String value) {
    terms.add(new ErlangString(value));
    return this;
  }

  public ArrayItems add (@NonNull Pid value) {
    terms.add(value);
    return this;
  }

  public ArrayItems add (@NonNull Port value) {
    terms.add(value);
    return this;
  }

  public ArrayItems add (@NonNull Reference value) {
    terms.add(value);
    return this;
  }

  public ArrayItems tuple (@NonNull ArrayItems items) {
    terms.add(items.toTuple());
    return this;
  }

  public ArrayItems list (@NonNull ArrayItems items) {
    terms.add(items.toList());
    return this;
  }

  public ArrayItems map (@NonNull MapItems items) {
    terms.add(items.toMap());
    return this;
  }

  Tuple toTuple () {
    return Tuple.builder()
        .adds(terms)
        .build();
  }

  ErlangList toList () {
    return new ErlangList(terms);
  }
}
