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
public class ErlangString extends ErlangTerm {

  String value;

  public ErlangString (TermType type) {
    super(type);
  }

  public ErlangString (String value) {
    this(value.length() <= 65535 && value.codePoints().allMatch(it -> it < 0 || it > 255)
       ? STRING
       : LIST);
    this.value = value;
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    val length = buffer.getShort();
    val bytes = buffer.getBytes(length);
    value = new String(bytes, ISO_8859_1);
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    val length = value.length();

    if (length == 0) {
      buffer.put(new Nil().toBytes());
    } else if (length <= 65535 && is8bitString(value)) {
      buffer.put2B((short) length);
      buffer.put(value.getBytes(ISO_8859_1));
    } else {
      val elements = value.codePoints()
          .boxed()
          .map(IntegralNumber::from)
          .toArray(IntegralNumber[]::new);

      buffer.put(ErlangList.builder()
          .elements(elements)
          .build()
          .toBytes()
      );
    }
  }

  private boolean is8bitString (String string) {
    return string.codePoints().allMatch(it -> it < 0 || it > 255);
  }
}
