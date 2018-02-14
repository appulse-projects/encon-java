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

import static io.appulse.encon.java.protocol.TermType.INTEGER;
import static io.appulse.encon.java.protocol.TermType.LARGE_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_BIG;
import static io.appulse.encon.java.protocol.TermType.SMALL_INTEGER;
import static java.math.BigInteger.ZERO;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;

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
public class ErlangInteger extends ErlangTerm {

  private static final int MAX_INTEGER;

  private static final int MIN_INTEGER;

  private static final int MIN_CACHE;

  private static final int MAX_CACHE;

  private static final ErlangInteger[] CACHE;

  static {
    MAX_INTEGER = (1 << 27) - 1;
    MIN_INTEGER = -(1 << 27) - 1;

    MIN_CACHE = -1;
    MAX_CACHE = 256;

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

  public ErlangInteger (TermType type) {
    super(type);
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
    this(SMALL_INTEGER);
    this.value = BigInteger.valueOf(value);
    setupType(value);
  }

  public ErlangInteger (BigInteger value) {
    this(SMALL_BIG);
    this.value = value;
    if (value.bitLength() < 64) {
      setupType(value.longValue());
    } else if (value.abs().toByteArray().length < 256) {
      setType(SMALL_BIG);
    } else {
      setType(LARGE_BIG);
    }
  }

  private void setupType (long longValue) {
    if ((longValue & 0xFFL) == longValue) {
      setType(SMALL_INTEGER);
    } else if (longValue >= MIN_INTEGER && longValue <= MAX_INTEGER) {
      setType(INTEGER);
    } else if (value.abs().toByteArray().length < 256) {
      setType(SMALL_BIG);
    }
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

  public byte[] asBinary(byte[] defaultValue) {
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
  protected void read (@NonNull Bytes buffer) {
    int arity = -1;
    int sign = -1;
    switch (getType()) {
    case SMALL_INTEGER:
      value = BigInteger.valueOf(buffer.getUnsignedByte());
      break;
    case INTEGER:
      value = BigInteger.valueOf(buffer.getInt());
      break;
    case SMALL_BIG:
      arity = buffer.getByte();
    case LARGE_BIG:
      if (arity == -1) {
        arity = buffer.getInt();
      }
      sign = buffer.getByte();
      byte[] bytes = buffer.getBytes(arity);
      // Reverse the array to make it big endian.
      for (int i = 0, j = bytes.length - 1; i < j; i++, j--) {
        // Swap [i] with [j]
        byte tmp = bytes[i];
        bytes[i] = bytes[j];
        bytes[j] = tmp;
      }
      value = new BigInteger(bytes);
      if (sign != 0) {
        value = value.negate();
      }
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }

  @Override
  protected void write (@NonNull Bytes buffer) {
    switch (getType()) {
    case SMALL_INTEGER:
      buffer.put1B(value.shortValue());
      break;
    case INTEGER:
      buffer.put4B(value.intValue());
      break;
    case SMALL_BIG:
    case LARGE_BIG:
      byte[] bytes = value.abs().toByteArray();
      int index = 0;
      for (; index < bytes.length && bytes[index] == 0; index++) {
        // skip leading zeros
      }

      byte[] magnitude = Arrays.copyOfRange(bytes, index, bytes.length);
      int length = magnitude.length;
      // Reverse the array to make it little endian.
      for (int i = 0, j = length; i < j--; i++) {
        // Swap [i] with [j]
        byte temp = magnitude[i];
        magnitude[i] = magnitude[j];
        magnitude[j] = temp;
      }

      if ((length & 0xFF) == length) {
        buffer.put1B(length); // length
      } else {
        buffer.put4B(length); // length
      }
      val sign = value.signum() < 0
                 ? 1
                 : 0;
      buffer.put1B(sign);
      buffer.put(magnitude);
      break;
    default:
      throw new IllegalArgumentException("");
    }
  }
}
