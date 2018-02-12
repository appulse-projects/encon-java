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

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigInteger;

import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangBinary;
import io.appulse.encon.java.protocol.type.ErlangString;
import io.appulse.encon.java.protocol.type.ErlangFloat;
import io.appulse.encon.java.protocol.type.ErlangInteger;
import io.appulse.encon.java.protocol.type.ErlangNil;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.encon.java.protocol.type.ErlangPort;
import io.appulse.encon.java.protocol.type.ErlangReference;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Value
@Getter(PACKAGE)
@RequiredArgsConstructor(access = PRIVATE)
public class MapItem {

  public static MapItem atom (boolean value) {
    return new MapItem(new ErlangAtom(value));
  }

  public static MapItem number (char value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem number (byte value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem binary (@NonNull byte[] value) {
    return new MapItem(new ErlangBinary(value));
  }

  public static MapItem number (short value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem number (int value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem number (long value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem number (@NonNull BigInteger value) {
    return new MapItem(new ErlangInteger(value));
  }

  public static MapItem number (float value) {
    return new MapItem(new ErlangFloat(value));
  }

  public static MapItem number (double value) {
    return new MapItem(new ErlangFloat(value));
  }

  public static MapItem atom (@NonNull String value) {
    return new MapItem(new ErlangAtom(value));
  }

  public static MapItem string (@NonNull String value) {
    return new MapItem(new ErlangString(value)); // List/BitString?
  }

  public static MapItem pid (@NonNull ErlangPid value) {
    return new MapItem(value);
  }

  public static MapItem port (@NonNull ErlangPort value) {
    return new MapItem(value);
  }

  public static MapItem reference (@NonNull ErlangReference value) {
    return new MapItem(value);
  }

  public static MapItem nil () {
    return new MapItem(new ErlangNil());
  }

  ErlangTerm term;
}
