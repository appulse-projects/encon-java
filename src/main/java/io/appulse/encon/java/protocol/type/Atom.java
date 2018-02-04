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

import static io.appulse.encon.java.protocol.TermType.ATOM_UTF8;
import static io.appulse.encon.java.protocol.TermType.SMALL_ATOM;
import static io.appulse.encon.java.protocol.TermType.SMALL_ATOM_UTF8;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

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
 * @since 0.0.1
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class Atom extends ErlangTerm {

  private static final int MAX_LENGTH = 255;

  String value;

  public Atom (TermType type) {
    super(type);
  }

  public Atom (String value) {
    this(value.getBytes(UTF_8).length < 256
       ? SMALL_ATOM_UTF8
       : ATOM_UTF8);

    this.value = trim(value);
  }

  public Atom (boolean value) {
    this(SMALL_ATOM_UTF8);
    this.value = Boolean.toString(value);
  }

  @Override
  public boolean asBoolean (boolean defaultValue) {
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public String asText (String defaultValue) {
    return value;
  }

  @Override
  public boolean isTextual () {
    return true;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    val type = getType();

    val length = type == SMALL_ATOM || type == SMALL_ATOM_UTF8
           ? buffer.getByte()
           : buffer.getShort();

    val charset = type == SMALL_ATOM_UTF8 || type == ATOM_UTF8
            ? UTF_8
            : ISO_8859_1;

    val bytes = buffer.getBytes(length);
    this.value = trim(new String(bytes, charset));
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    val bytes = value.getBytes(UTF_8);

    switch (getType()) {
    case SMALL_ATOM_UTF8:
      buffer.put1B(bytes.length);
      break;
    case ATOM_UTF8:
      buffer.put2B(bytes.length);
      break;
    default:
      throw new IllegalArgumentException();
    }
    buffer.put(bytes);
  }

  private String trim (String atom) {
    return atom.codePointCount(0, atom.length()) <= MAX_LENGTH
         ? atom
         // Throwing an exception would be better I think, but truncation
         // seems to be the way it has been done in other parts of OTP...
         : new String(atom.codePoints().toArray(), 0, MAX_LENGTH);
  }
}
