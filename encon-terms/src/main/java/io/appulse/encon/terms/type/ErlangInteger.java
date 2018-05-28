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

import static io.appulse.encon.terms.TermType.INTEGER;
import static io.appulse.encon.terms.TermType.LARGE_BIG;
import static io.appulse.encon.terms.TermType.SMALL_BIG;
import static io.appulse.encon.terms.TermType.SMALL_INTEGER;
import static java.math.BigInteger.ZERO;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;

import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.ErlangTermDecodeException;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

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
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangInteger extends ErlangTerm {

  private static final long serialVersionUID = -1757584303003802030L;

  private static final int MAX_SMALL_INTEGER;

  private static final int MAX_INTEGER;

  private static final int MIN_INTEGER;

  private static final int MIN_CACHE;

  private static final int MAX_CACHE;

  private static final int MAX_SMALL_BIG_BYTES_LENGTH;

  private static final ErlangInteger[] CACHE;

  static {
    MAX_SMALL_INTEGER = 255;

    MAX_INTEGER = (1 << 27) - 1;
    MIN_INTEGER = -(1 << 27) - 1;

    MIN_CACHE = -1;
    MAX_CACHE = 256;

    MAX_SMALL_BIG_BYTES_LENGTH = 255;

    CACHE = IntStream.range(MIN_CACHE, MAX_CACHE)
        .boxed()
        .map(ErlangInteger::new)
        .toArray(ErlangInteger[]::new);
  }

  public static ErlangInteger from (int number) {
    return number > MAX_CACHE || number < MIN_CACHE
           ? new ErlangInteger(number)
           : CACHE[number - MIN_CACHE];
  }

  BigInteger value;

  public ErlangInteger (TermType type, @NonNull ByteBuf buffer) {
    super(type);

    switch (type) {
    case SMALL_INTEGER:
      value = BigInteger.valueOf(buffer.readUnsignedByte());
      break;
    case INTEGER:
      value = BigInteger.valueOf(buffer.readInt());
      break;
    case SMALL_BIG:
    case LARGE_BIG:
      val arity = type == SMALL_BIG
                  ? buffer.readByte()
                  : buffer.readInt();

      val sign = buffer.readByte();

      val bytes = new byte[arity];
      buffer.readBytes(bytes);
      reverse(bytes);

      value = sign == 0
              ? new BigInteger(bytes)
              : new BigInteger(bytes).negate();
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), type);
    }
  }

  public ErlangInteger (char value) {
    this((long) value);
  }

  public ErlangInteger (byte value) {
    this((long) (value & 0xFFL));
  }

  public ErlangInteger (short value) {
    this((long) value);
  }

  public ErlangInteger (int value) {
    this((long) value);
  }

  public ErlangInteger (long value) {
    super();
    this.value = BigInteger.valueOf(value);
    setupType(value);
  }

  public ErlangInteger (@NonNull BigInteger value) {
    super();
    this.value = value;
    if (value.bitLength() < Long.BYTES) {
      setupType(value.longValue());
    } else if (value.abs().toByteArray().length <= MAX_SMALL_BIG_BYTES_LENGTH) {
      setType(SMALL_BIG);
    } else {
      setType(LARGE_BIG);
    }
  }

  @Override
  public boolean isByte () {
    return (value.bitLength() + 1) <= Byte.SIZE;
  }

  @Override
  public boolean isShort () {
    return (value.bitLength() + 1) <= Short.SIZE;
  }

  @Override
  public boolean isInt () {
    return (value.bitLength() + 1) <= java.lang.Integer.SIZE;
  }

  @Override
  public boolean isLong () {
    return (value.bitLength() + 1) <= Long.SIZE;
  }

  @Override
  public boolean isBigInteger () {
    return true;
  }

  @Override
  public Number asNumber () {
    return value;
  }

  @Override
  public byte[] asBinary (byte[] defaultValue) {
    return value.toByteArray();
  }

  @Override
  public boolean asBoolean (boolean defaultValue) {
    return value.equals(ZERO);
  }

  @Override
  public String asText (String defaultValue) {
    return value.toString();
  }

  @Override
  public byte asByte (byte defaultValue) {
    return value.byteValue();
  }

  @Override
  public short asShort (short defaultValue) {
    return value.shortValue();
  }

  @Override
  public int asInt (int defaultValue) {
    return value.intValue();
  }

  @Override
  public long asLong (long defaultValue) {
    return value.longValue();
  }

  @Override
  public BigInteger asBigInteger (BigInteger defaultValue) {
    return value;
  }

  @Override
  public float asFloat (float defaultValue) {
    return value.floatValue();
  }

  @Override
  public double asDouble (double defaultValue) {
    return value.doubleValue();
  }

  @Override
  public BigDecimal asDecimal (BigDecimal defaultValue) {
    return new BigDecimal(value);
  }

  @Override
  protected void serialize (ByteBuf buffer) {
    switch (getType()) {
    case SMALL_INTEGER:
      buffer.writeByte(value.shortValue());
      break;
    case INTEGER:
      buffer.writeInt(value.intValue());
      break;
    case SMALL_BIG:
    case LARGE_BIG:
      byte[] bytes = value.abs().toByteArray();
      int index = 0;
      for (; index < bytes.length && bytes[index] == 0; index++) {
        // skip leading zeros
      }

      byte[] magnitude = Arrays.copyOfRange(bytes, index, bytes.length);
      reverse(magnitude);

      int length = magnitude.length;
      if ((length & 0xFF) == length) {
        buffer.writeByte(length); // length
      } else {
        buffer.writeInt(length); // length
      }
      val sign = value.signum() < 0
                 ? 1
                 : 0;
      buffer.writeByte(sign);
      buffer.writeBytes(magnitude);
      break;
    default:
      throw new IllegalErlangTermTypeException(getClass(), getType());
    }
  }

  private void setupType (long longValue) {
    if ((longValue & MAX_SMALL_INTEGER) == longValue) {
      setType(SMALL_INTEGER);
    } else if (longValue >= MIN_INTEGER && longValue <= MAX_INTEGER) {
      setType(INTEGER);
    } else if (value.abs().toByteArray().length <= MAX_SMALL_BIG_BYTES_LENGTH) {
      setType(SMALL_BIG);
    } else {
      throw new ErlangTermDecodeException();
    }
  }

  private void reverse (byte[] data) {
    int left = 0;
    int right = data.length - 1;
    while (left < right) {
      byte temp = data[left];
      data[left] = data[right];
      data[right] = temp;

      left++;
      right--;
    }
  }
}
