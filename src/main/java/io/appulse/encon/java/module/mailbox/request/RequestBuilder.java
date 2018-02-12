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

import io.appulse.encon.java.module.mailbox.Mailbox;
import java.math.BigInteger;

import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangBinary;
import io.appulse.encon.java.protocol.type.ErlangString;
import io.appulse.encon.java.protocol.type.ErlangFloat;
import io.appulse.encon.java.protocol.type.ErlangInteger;
import io.appulse.encon.java.protocol.type.ErlangNil;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangPort;
import io.appulse.encon.java.protocol.type.ErlangReference;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RequestBuilder {

  Mailbox mailbox;

  public RequestInvoker put (boolean value) {
    return new RequestInvoker(mailbox, new ErlangAtom(value));
  }

  public RequestInvoker put (char value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (byte value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (@NonNull byte[] value) {
    return new RequestInvoker(mailbox, new ErlangBinary(value));
  }

  public RequestInvoker put (short value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (int value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (long value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (@NonNull BigInteger value) {
    return new RequestInvoker(mailbox, new ErlangInteger(value));
  }

  public RequestInvoker put (float value) {
    return new RequestInvoker(mailbox, new ErlangFloat(value));
  }

  public RequestInvoker put (double value) {
    return new RequestInvoker(mailbox, new ErlangFloat(value));
  }

  public RequestInvoker putAtom (@NonNull String value) {
    return new RequestInvoker(mailbox, new ErlangAtom(value));
  }

  public RequestInvoker putNil () {
    return new RequestInvoker(mailbox, new ErlangNil());
  }

  public RequestInvoker put (@NonNull String value) {
    return new RequestInvoker(mailbox, new ErlangString(value)); // List/BitString?
  }

  public RequestInvoker put (@NonNull ErlangPid value) {
    return new RequestInvoker(mailbox, value);
  }

  public RequestInvoker put (@NonNull ErlangPort value) {
    return new RequestInvoker(mailbox, value);
  }

  public RequestInvoker put (@NonNull ErlangReference value) {
    return new RequestInvoker(mailbox, value);
  }

  public AbstractArrayRequestBuilder makeTuple () {
    return new TupleRequestBuilder(mailbox);
  }

  public AbstractArrayRequestBuilder makeList () {
    return new ListRequestBuilder(mailbox);
  }

  public MapRequestBuilder makeMap () {
    return new MapRequestBuilder(mailbox);
  }
}
