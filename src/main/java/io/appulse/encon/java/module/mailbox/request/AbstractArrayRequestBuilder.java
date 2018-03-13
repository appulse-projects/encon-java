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

package io.appulse.encon.java.module.mailbox.request;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangBinary;
import io.appulse.encon.java.protocol.type.ErlangFloat;
import io.appulse.encon.java.protocol.type.ErlangInteger;
import io.appulse.encon.java.protocol.type.ErlangNil;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangPort;
import io.appulse.encon.java.protocol.type.ErlangReference;
import io.appulse.encon.java.protocol.type.ErlangString;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class AbstractArrayRequestBuilder extends AbstractPrepareMessageBeforeSendRequestBuilder {

  @Getter(PROTECTED)
  List<ErlangTerm> terms;

  AbstractArrayRequestBuilder (@NonNull Mailbox mailbox) {
    super(mailbox);
    terms = new LinkedList<>();
  }

  public AbstractArrayRequestBuilder add (boolean value) {
    terms.add(new ErlangAtom(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (char value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (byte value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull byte[] value) {
    terms.add(new ErlangBinary(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (short value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (int value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (long value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull BigInteger value) {
    terms.add(new ErlangInteger(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (float value) {
    terms.add(new ErlangFloat(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (double value) {
    terms.add(new ErlangFloat(value));
    return this;
  }

  public AbstractArrayRequestBuilder addAtom (@NonNull String value) {
    terms.add(new ErlangAtom(value));
    return this;
  }

  public AbstractArrayRequestBuilder addNil () {
    terms.add(new ErlangNil());
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull String value) {
    terms.add(new ErlangString(value));
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull ErlangPid value) {
    terms.add(value);
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull ErlangPort value) {
    terms.add(value);
    return this;
  }

  public AbstractArrayRequestBuilder add (@NonNull ErlangReference value) {
    terms.add(value);
    return this;
  }

  public AbstractArrayRequestBuilder addTuple (@NonNull ArrayItems items) {
    terms.add(items.toTuple());
    return this;
  }

  public AbstractArrayRequestBuilder addList (@NonNull ArrayItems items) {
    terms.add(items.toList());
    return this;
  }

  public AbstractArrayRequestBuilder addMap (@NonNull MapItems items) {
    terms.add(items.toMap());
    return this;
  }
}
