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

import static io.appulse.encon.java.protocol.TermType.BIT_BINNARY;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.Erlang;
import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.exception.ErlangTermDecodeException;
import io.appulse.encon.java.protocol.exception.ErlangTermValidationException;
import io.appulse.encon.java.protocol.term.ErlangTerm;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Getter
@ToString
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class ErlangBitString extends ErlangTerm {

  private static final long serialVersionUID = 7484207266013629164L;

  byte[] bits;

  int pad;

  public ErlangBitString (TermType type) {
    super(type);
  }

  @Builder
  public ErlangBitString (@NonNull byte[] bits, int pad) {
    this(BIT_BINNARY);

    this.bits = new byte[bits.length];
    System.arraycopy(bits, 0, this.bits, 0, bits.length);

    this.pad = pad;

    validate();
  }

  /**
   * Returns bits array clone.
   *
   * @return bits array
   */
  public byte[] getBits () {
    return bits.clone();
  }

  @Override
  protected void read (ByteBuf buffer) {
    val length = buffer.readInt();
    val tail = buffer.readByte();

    if (tail < 0 || tail > 7) {
      throw new ErlangTermDecodeException("Wrong tail bit count: " + tail);
    }
    if (length == 0 && tail != 0) {
      throw new ErlangTermDecodeException("Length 0 on BitString with tail bit count: " + tail);
    }

    bits = new byte[length];
    buffer.readBytes(bits);
    pad = 8 - tail;

    validate();
  }

  @Override
  protected void write (ByteBuf buffer) {
    if (pad == 0) {
      val position = buffer.writerIndex();
      buffer.writerIndex(position - 1);
      Erlang.binary(bits).writeTo(buffer);
      return;
    }
    buffer.writeInt(bits.length);
    buffer.writeByte(8 - pad);
    buffer.writeBytes(bits);
  }

  private void validate () {
    if (pad < 0 || pad > 7) {
      throw new ErlangTermValidationException("Padding must be in range 0..7");
    }
    if (pad != 0 && bits.length == 0) {
      throw new ErlangTermValidationException("Padding on zero length BitString");
    }
    if (bits.length != 0) {
      // make sure padding is zero
      bits[bits.length - 1] &= ~((1 << pad) - 1);
    }
  }
}
