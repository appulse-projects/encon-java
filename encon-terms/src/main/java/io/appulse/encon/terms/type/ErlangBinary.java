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

import static io.appulse.encon.terms.TermType.BINARY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Provides a Java representation of Erlang binaries. Anything that can be
 * represented as a sequence of bytes can be made into an Erlang binary.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangBinary extends ErlangTerm {

  private static final long serialVersionUID = 2120051138040192507L;

  byte[] bytes;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangBinary (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    val length = buffer.readInt();
    bytes = new byte[length];
    buffer.readBytes(bytes);
  }

  /**
   * Constructs Erlang's binary object.
   *
   * @param bytes object's binary value
   */
  public ErlangBinary (@NonNull byte[] bytes) {
    super(BINARY);

    this.bytes = new byte[bytes.length];
    System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
  }

  @Override
  public byte[] asBinary (byte[] defaultValue) {
    return bytes.clone();
  }

  @Override
  public String asText (String defaultValue) {
    return new String(bytes, UTF_8);
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    buffer.writeInt(bytes.length);
    buffer.writeBytes(bytes);
  }
}
