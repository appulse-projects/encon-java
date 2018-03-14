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

import static io.appulse.encon.java.protocol.TermType.BINARY;
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
 * @since 1.0.0
 */
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangBinary extends ErlangTerm {

  private static final long serialVersionUID = 2120051138040192507L;

  byte[] bytes;

  public ErlangBinary (TermType type) {
    super(type);
  }

  public ErlangBinary (@NonNull byte[] bytes) {
    this(BINARY);

    this.bytes = new byte[bytes.length];
    System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
  }

  @Override
  public byte[] asBinary (byte[] defaultValue) {
    return bytes.clone();
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    val length = buffer.getInt();
    bytes = buffer.getBytes(length);
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    buffer.put4B(bytes.length);
    buffer.put(bytes);
  }
}
