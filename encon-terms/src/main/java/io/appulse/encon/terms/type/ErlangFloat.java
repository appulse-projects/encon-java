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

import static io.appulse.encon.terms.TermType.NEW_FLOAT;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Erlang's floating point number representation.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangFloat extends ErlangTerm {

  private static final long serialVersionUID = -4146479045850295285L;

  double value;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
  public ErlangFloat (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    switch (type) {
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
      throw new IllegalErlangTermTypeException(getClass(), type);
    }
  }

  /**
   * Constructs Erlang's floating point number object with specific {@code double} value.
   *
   * @param value {@code double} number's value
   */
  public ErlangFloat (double value) {
    super(NEW_FLOAT);
    this.value = value;
  }

  /**
   * Constructs Erlang's floating point number object with specific {@code float} value.
   *
   * @param value {@code float} number's value
   */
  public ErlangFloat (float value) {
    super(NEW_FLOAT);
    this.value = value;
  }

  /**
   * Constructs Erlang's floating point number object with specific {@link BigDecimal} value.
   *
   * @param value {@link BigDecimal} number's value
   */
  public ErlangFloat (BigDecimal value) {
    super(NEW_FLOAT);
    this.value = value.doubleValue();
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
  protected void serialize (ByteBuf buffer) {
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
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }
}
