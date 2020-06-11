/*
 * Copyright 2020 the original author or authors.
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

import static io.appulse.encon.terms.TermType.ATOM_UTF8;
import static io.appulse.encon.terms.TermType.SMALL_ATOM;
import static io.appulse.encon.terms.TermType.SMALL_ATOM_UTF8;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

import java.nio.charset.Charset;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * An atom is a literal, a constant with name. An atom is to be enclosed in
 * single quotes (') if it does not begin with a lower-case letter or
 * if it contains other characters than alphanumeric characters,
 * underscore (_), or @.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@SuppressWarnings("deprecation")
@EqualsAndHashCode(
    of = "bytes",
    callSuper = false,
    doNotUseGetters = false
)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangAtom extends ErlangTerm {

  private static final long serialVersionUID = -2748345367418129439L;

  /**
   * The {@code ErlangAtom} object corresponding to the atom
   * value {@code true}.
   */
  public static final ErlangAtom ATOM_TRUE = new ErlangAtom(true);

  /**
   * The {@code ErlangAtom} object corresponding to the atom
   * value {@code false}.
   */
  public static final ErlangAtom ATOM_FALSE = new ErlangAtom(false);

  private static final int MAX_ATOM_CODE_POINTS_LENGTH = 255;

  private static final int MAX_SMALL_ATOM_BYTES_LENGTH = 255;

  @Getter(lazy = true, value = PRIVATE)
  String value = createString();

  byte[] bytes;

  transient Charset charset;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangAtom (TermType type, ByteBuf buffer) {
    super(type);

    int length = type == SMALL_ATOM || type == SMALL_ATOM_UTF8
                 ? buffer.readUnsignedByte()
                 : buffer.readUnsignedShort();

    charset = type == SMALL_ATOM_UTF8 || type == ATOM_UTF8
              ? UTF_8
              : ISO_8859_1;

    bytes = new byte[length];
    buffer.readBytes(bytes);
  }

  /**
   * Constructs Erlang's atom object with specific {@code boolean} value.
   *
   * @param value {@code boolean} atom's value
   */
  public ErlangAtom (boolean value) {
    this(Boolean.toString(value), UTF_8);
  }

  /**
   * Constructs Erlang's atom object with specific {@link String} value.
   *
   * @param value {@link String} atom's value
   */
  public ErlangAtom (String value) {
    this(value, UTF_8);
  }

  // @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public ErlangAtom (@NonNull String value, @NonNull Charset charset) {
    super();

    if (value.codePointCount(0, value.length()) <= MAX_ATOM_CODE_POINTS_LENGTH) {
      this.value.set(value);
    } else {
      // Throwing an exception would be better I think, but truncation
      // seems to be the way it has been done in other parts of OTP...
      this.value.set(new String(value.codePoints().toArray(), 0, MAX_ATOM_CODE_POINTS_LENGTH));
    }

    this.charset = charset;
    this.bytes = getValue().getBytes(charset);
    if (bytes.length > MAX_SMALL_ATOM_BYTES_LENGTH) {
      setType(ATOM_UTF8);
    } else {
      setType(SMALL_ATOM_UTF8);
    }
  }

  @Override
  public boolean isBoolean () {
    return "true".equalsIgnoreCase(asText()) || "false".equalsIgnoreCase(asText());
  }

  @Override
  public boolean asBoolean (boolean defaultValue) {
    return "true".equalsIgnoreCase(asText());
  }

  @Override
  public String asText (String defaultValue) {
    return getValue();
  }

  @Override
  public byte[] asBinary (byte[] defaultValue) {
    return bytes.clone();
  }

  @Override
  public ErlangAtom asAtom () {
    return this;
  }

  @Override
  public boolean isTextual () {
    return true;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    switch (getType()) {
    case SMALL_ATOM_UTF8:
    case SMALL_ATOM:
      buffer.writeByte(bytes.length);
      break;
    case ATOM_UTF8:
    case ATOM:
      buffer.writeShort(bytes.length);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
    buffer.writeBytes(bytes);
  }

  private String createString () {
    return new String(bytes, charset);
  }
}
