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

import static io.appulse.encon.terms.TermType.ATOM_UTF8;
import static io.appulse.encon.terms.TermType.SMALL_ATOM;
import static io.appulse.encon.terms.TermType.SMALL_ATOM_UTF8;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;
import static lombok.AccessLevel.PRIVATE;

import java.nio.charset.Charset;
import java.util.Arrays;

import io.appulse.encon.common.LruCache;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

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
@EqualsAndHashCode(callSuper = true, of = "bytes")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangAtom extends ErlangTerm {

  private static final long serialVersionUID = -2748345367418129439L;

  private static final int MAX_ATOM_CODE_POINTS_LENGTH = 255;

  private static final int MAX_SMALL_ATOM_BYTES_LENGTH = 255;

  private static final LruCache<Integer, ErlangAtom> CACHE = new LruCache<>(1000);

  private static final ErlangAtom ATOM_TRUE = cached(Boolean.TRUE.toString().toLowerCase(ENGLISH));

  private static final ErlangAtom ATOM_FALSE = cached(Boolean.FALSE.toString().toLowerCase(ENGLISH));

  public static ErlangAtom cached (boolean value) {
    return value
           ? ATOM_TRUE
           : ATOM_FALSE;
  }

  public static ErlangAtom cached (String value) {
    Charset charset = UTF_8;
    byte[] bytes = value.getBytes(charset);
    int hashCode = Arrays.hashCode(bytes);
    return CACHE.computeIfAbsent(hashCode, key -> new ErlangAtom(value, charset, bytes));
  }

  @SuppressWarnings("deprecation")
  public static ErlangAtom cached (TermType type, ByteBuf buffer) {
    ByteArrayHashCode byteProcessor = new ByteArrayHashCode();

    int length = type == SMALL_ATOM || type == SMALL_ATOM_UTF8
                 ? buffer.readUnsignedByte()
                 : buffer.readUnsignedShort();

    buffer.forEachByte(buffer.readerIndex(), length, byteProcessor);

    return CACHE.compute(byteProcessor.getHashCode(), (key, value) -> {
      if (value == null) {
        return new ErlangAtom(type, buffer, length);
      } else {
        buffer.skipBytes(length);
        return value;
      }
    });
  }

  @NonFinal
  String value;

  byte[] bytes;

  transient Charset charset;

  /**
   * Constructs Erlang's atom object with specific {@link String} value.
   *
   * @param value {@link String} atom's value
   */
  public ErlangAtom (@NonNull String value) {
    super();

    this.value = value.codePointCount(0, value.length()) <= MAX_ATOM_CODE_POINTS_LENGTH
                 ? value
                 // Throwing an exception would be better I think, but truncation
                 // seems to be the way it has been done in other parts of OTP...
                 : new String(value.codePoints().toArray(), 0, MAX_ATOM_CODE_POINTS_LENGTH);

    charset = UTF_8;
    bytes = this.value.getBytes(charset);
    if (bytes.length > MAX_SMALL_ATOM_BYTES_LENGTH) {
      setType(ATOM_UTF8);
    } else {
      setType(SMALL_ATOM_UTF8);
    }
  }

  /**
   * Constructs Erlang's atom object with specific {@code boolean} value.
   *
   * @param value {@code boolean} atom's value
   */
  public ErlangAtom (boolean value) {
    super(SMALL_ATOM_UTF8);
    charset = UTF_8;
    this.value = Boolean.toString(value);
    bytes = value
            ? Boolean.TRUE.toString().getBytes(charset)
            : Boolean.FALSE.toString().getBytes(charset);
  }

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   *
   * @param length amount of useful bytes
   */
  private ErlangAtom (TermType type, ByteBuf buffer, int length) {
    super(type);

    charset = type == SMALL_ATOM_UTF8 || type == ATOM_UTF8
              ? UTF_8
              : ISO_8859_1;

    bytes = new byte[length];
    buffer.readBytes(bytes);
  }

  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  private ErlangAtom (String value, Charset charset, byte[] bytes) {
    super();

    this.value = value.codePointCount(0, value.length()) <= MAX_ATOM_CODE_POINTS_LENGTH
                 ? value
                 // Throwing an exception would be better I think, but truncation
                 // seems to be the way it has been done in other parts of OTP...
                 : new String(value.codePoints().toArray(), 0, MAX_ATOM_CODE_POINTS_LENGTH);

    this.charset = charset;
    this.bytes = bytes;
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
    if (value == null) {
      value = new String(bytes, charset);
    }
    return value;
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
      buffer.writeByte(bytes.length);
      break;
    case ATOM_UTF8:
      buffer.writeShort(bytes.length);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
    buffer.writeBytes(bytes);
  }

  @Getter
  @FieldDefaults(level = PRIVATE)
  private static class ByteArrayHashCode implements ByteProcessor {

    int hashCode = 1;

    @Override
    public boolean process (byte value) throws Exception {
      hashCode = 31 * hashCode + value;
      return true;
    }
  }
}
