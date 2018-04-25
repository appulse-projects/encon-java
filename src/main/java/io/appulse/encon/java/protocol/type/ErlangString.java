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

import static io.appulse.encon.java.protocol.TermType.LIST;
import static io.appulse.encon.java.protocol.TermType.STRING;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.Erlang;
import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangString extends ErlangTerm {

  private static final long serialVersionUID = -606017265338010507L;

  public static ErlangString string (@NonNull String value) {
    return new ErlangString(value);
  }

  String value;

  public ErlangString (TermType type) {
    super(type);
  }

  public ErlangString (String value) {
    this(STRING);
    this.value = value;

    if (value.length() > 65535 || !is8bitString()) {
      setType(LIST);
    }
  }

  @Override
  public String asText () {
    return value;
  }

  @Override
  protected void read (ByteBuf buffer) {
    val length = buffer.readShort();
    value = buffer.readCharSequence(length, ISO_8859_1).toString();
  }

  @Override
  protected void write (ByteBuf buffer) {
    val positionBefore = buffer.writerIndex() - 1;
    if (value.isEmpty()) {
      buffer.writerIndex(positionBefore);
      Erlang.NIL.writeTo(buffer);
      return;
    }

    val length = value.length();
    switch (getType()) {
    case STRING:
      buffer.writeShort(length);
      buffer.writeCharSequence(value, ISO_8859_1);
      break;
    case LIST:
      val elements = value.codePoints()
          .boxed()
          .map(ErlangInteger::from)
          .toArray(ErlangInteger[]::new);

      buffer.writerIndex(positionBefore);
      Erlang.list(elements).writeTo(buffer);
      break;
    default:
      buffer.writerIndex(positionBefore);
      Erlang.NIL.writeTo(buffer);
    }
  }

  private boolean is8bitString () {
    return value.codePoints().allMatch(it -> it >= 0 && it <= 255);
  }
}
