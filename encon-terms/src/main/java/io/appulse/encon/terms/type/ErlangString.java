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

import static io.appulse.encon.terms.TermType.LIST;
import static io.appulse.encon.terms.TermType.STRING;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.terms.Erlang;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Strings are enclosed in double quotes ("), but is not a data type in Erlang.
 * Instead, a string {@code "hello"} is shorthand for the list {@code [$h,$e,$l,$l,$o]},
 * that is, {@code [104,101,108,108,111]}.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangString extends ErlangTerm {

  private static final long serialVersionUID = -606017265338010507L;

  String value;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangString (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val length = buffer.readShort();
    value = buffer.readCharSequence(length, ISO_8859_1).toString();
  }

  /**
   * Constructs Erlang's string object.
   *
   * @param value object's {@link String} value
   */
  public ErlangString (String value) {
    super();

    this.value = value;
    if (value.length() > 65535 || !is8bitString()) {
      setType(LIST);
    } else {
      setType(STRING);
    }
  }

  @Override
  public String asText () {
    return value;
  }

  @Override
  protected void serialize (ByteBuf buffer) {
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
          .map(ErlangInteger::cached)
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
