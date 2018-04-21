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

import static io.appulse.encon.java.protocol.TermType.NEW_FLOAT;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;
import io.netty.buffer.ByteBuf;
import java.math.BigDecimal;
import java.math.BigInteger;
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
public class ErlangFloat extends ErlangTerm {

  private static final long serialVersionUID = -4146479045850295285L;

  double value;

  public ErlangFloat (TermType type) {
    super(type);
  }

  public ErlangFloat (double value) {
    this(NEW_FLOAT);
    this.value = value;
  }

  public ErlangFloat (float value) {
    this(NEW_FLOAT);
    this.value = value;
  }

  @Override
  public boolean isFloat () {
    return true;
  }

  @Override
  public boolean isDouble () {
    return true;
  }

  @Override
  public boolean isBigDecimal () {
    return true;
  }

  @Override
  public Number asNumber () {
    return value;
  }

  @Override
  public String asText (String defaultValue) {
    return Double.toString(value);
  }

  @Override
  public short asShort (short defaultValue) {
    return (short) value;
  }

  @Override
  public int asInt (int defaultValue) {
    return (int) value;
  }

  @Override
  public long asLong (long defaultValue) {
    return (long) value;
  }

  @Override
  public BigInteger asBigInteger (BigInteger defaultValue) {
    return asDecimal().toBigInteger();
  }

  @Override
  public float asFloat (float defaultValue) {
    return (float) value;
  }

  @Override
  public double asDouble (double defaultValue) {
    return value;
  }

  @Override
  public BigDecimal asDecimal (BigDecimal defaultValue) {
    return BigDecimal.valueOf(value);
  }

  @Override
  protected void read (@NonNull Bytes buffer) {
    switch (getType()) {
    case FLOAT:
      val bytes = buffer.getBytes(31);
      val string = new String(bytes, ISO_8859_1);
      value = Double.valueOf(string);
      break;
    case NEW_FLOAT:
      val bits = buffer.getLong();
      value = Double.longBitsToDouble(bits);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }

  @Override
  protected void read (ByteBuf buffer) {
    switch (getType()) {
    case FLOAT:
      val bytes = new byte[31];
      buffer.readBytes(bytes);
      val string = new String(bytes, ISO_8859_1);
      value = Double.valueOf(string);
      break;
    case NEW_FLOAT:
      val bits = buffer.readLong();
      value = Double.longBitsToDouble(bits);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    switch (getType()) {
    case FLOAT:
      val string = String.format("%031.20e", value);
      val bytes = string.getBytes(ISO_8859_1);
      buffer.put(bytes);
      break;
    case NEW_FLOAT:
      val bits = Double.doubleToLongBits(value);
      buffer.put8B(bits);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }

  @Override
  protected void write (ByteBuf buffer) {
    switch (getType()) {
    case FLOAT:
      val string = String.format("%031.20e", value);
      buffer.writeCharSequence(string, ISO_8859_1);
      break;
    case NEW_FLOAT:
      val bits = Double.doubleToLongBits(value);
      buffer.writeLong(bits);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }
}
