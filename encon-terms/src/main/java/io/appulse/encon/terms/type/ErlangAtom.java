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
import static lombok.AccessLevel.PRIVATE;

import java.nio.charset.Charset;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangAtom extends ErlangTerm {

  private static final long serialVersionUID = -2748345367418129439L;

  private static final byte[] TRUE_BYTES = Boolean.TRUE.toString().getBytes(UTF_8);

  private static final byte[] FALSE_BYTES = Boolean.FALSE.toString().getBytes(UTF_8);

  private static final int MAX_ATOM_CODE_POINTS_LENGTH = 255;

  private static final int MAX_SMALL_ATOM_BYTES_LENGTH = 255;

  @NonFinal
  String value;

  byte[] bytes;

  transient Charset charset;

  @SuppressWarnings("deprecation")
  public ErlangAtom (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val length = type == SMALL_ATOM || type == SMALL_ATOM_UTF8
                 ? buffer.readUnsignedByte()
                 : buffer.readUnsignedShort();

    charset = type == SMALL_ATOM_UTF8 || type == ATOM_UTF8
              ? UTF_8
              : ISO_8859_1;

    bytes = new byte[length];
    buffer.readBytes(bytes);
  }

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

  public ErlangAtom (boolean value) {
    super(SMALL_ATOM_UTF8);
    charset = UTF_8;
    this.value = Boolean.toString(value);
    bytes = value
            ? TRUE_BYTES
            : FALSE_BYTES;
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
}
