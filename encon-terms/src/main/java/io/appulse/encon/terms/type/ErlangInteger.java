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

import io.appulse.encon.common.LruCache;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.TermType;
import io.appulse.encon.terms.exception.ErlangTermDecodeException;
import io.appulse.encon.terms.exception.IllegalErlangTermTypeException;

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

/**
 * Erlang's integer number representation.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ErlangInteger extends ErlangTerm {

  private static final long serialVersionUID = -1757584303003802030L;

  private static final int MAX_SMALL_INTEGER = 255;

  private static final int MAX_INTEGER = (1 << 27) - 1;

  private static final int MIN_INTEGER = -(1 << 27) - 1;

  private static final int MAX_SMALL_BIG_BYTES_LENGTH = 255;

  private static final LruCache<Integer, ErlangInteger> CACHE = new LruCache<>(1000);

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (byte value) {
    int hashCode = 31 + value;

    ErlangInteger result = CACHE.get(hashCode);
    if (result != null) {
      return result;
    }
    result = new ErlangInteger(value);
    CACHE.put(hashCode, result);
    return result;
  }

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (char value) {
    int hashCode = 31 + (byte) (value >> 8);
    hashCode = 31 * hashCode + (byte) value;

    ErlangInteger result = CACHE.get(hashCode);
    if (result != null) {
      return result;
    }
    result = new ErlangInteger(value);
    CACHE.put(hashCode, result);
    return result;
  }

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (short value) {
    int hashCode = 31 + (byte) (value >> 8);
    hashCode = 31 * hashCode + (byte) value;

    ErlangInteger result = CACHE.get(hashCode);
    if (result != null) {
      return result;
    }
    result = new ErlangInteger(value);
    CACHE.put(hashCode, result);
    return result;
  }

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (int value) {
    int hashCode = 31 + (byte) (value >> 24);
    hashCode = 31 * hashCode + (byte) (value >> 16);
    hashCode = 31 * hashCode + (byte) (value >> 8);
    hashCode = 31 * hashCode + (byte) value;

    ErlangInteger result = CACHE.get(hashCode);
    if (result != null) {
      return result;
    }
    result = new ErlangInteger(value);
    CACHE.put(hashCode, result);
    return result;
  }

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (long value) {
    int hashCode = 31 + (byte) (value >> 56);
    hashCode = 31 * hashCode + (byte) (value >> 48);
    hashCode = 31 * hashCode + (byte) (value >> 40);
    hashCode = 31 * hashCode + (byte) (value >> 32);
    hashCode = 31 * hashCode + (byte) (value >> 24);
    hashCode = 31 * hashCode + (byte) (value >> 16);
    hashCode = 31 * hashCode + (byte) (value >> 8);
    hashCode = 31 * hashCode + (byte) value;

    ErlangInteger result = CACHE.get(hashCode);
    if (result != null) {
      return result;
    }
    result = new ErlangInteger(value);
    CACHE.put(hashCode, result);
    return result;
  }

  /**
   * Creates cached {@link ErlangInteger} value.
   *
   * @param number integer value
   *
   * @return new or cached {@link ErlangInteger} object
   */
  public static ErlangInteger cached (BigInteger value) {
    if (value.bitLength() < Long.BYTES) {
      return cached(value.longValue());
    }
    // int hashCode = value.signum() == -1
    //                ? 32
    //                : 31;
    // return CACHE.computeIfAbsent(hashCode, key -> new ErlangInteger(value));
    return new ErlangInteger(value);
  }

  public static ErlangInteger cached (TermType type, @NonNull ByteBuf buffer) {
    int index = buffer.readerIndex();
    ByteArrayHashCode byteProcessor = new ByteArrayHashCode();

    int length;
    switch (type) {
    case SMALL_INTEGER:
      length = Byte.BYTES;
      break;
    case INTEGER:
      length = Integer.BYTES;
      break;
    case SMALL_BIG:
      length = buffer.readByte() + Byte.BYTES;
      break;
    case LARGE_BIG:
    default:
      length = buffer.readInt() + Byte.BYTES;
    }

    buffer.forEachByte(buffer.readerIndex(), length, byteProcessor);

    return CACHE.compute(byteProcessor.getHashCode(), (key, value) -> {
      if (value == null) {
        buffer.readerIndex(index);
        return new ErlangInteger(type, buffer);
      } else {
        buffer.skipBytes(length);
        return value;
      }
    });
  }

  BigInteger value;

  @NonFinal
  byte[] cachedMagnitude;

  /**
   * Constructs Erlang's term object with specific {@link TermType} from {@link ByteBuf}.
   *
   * @param type   object's type
   *
   * @param buffer byte buffer
   */
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

  /**
   * Constructs Erlang's integer number object with specific {@code char} value.
   *
   * @param value {@code char} number's value
   */
  public ErlangInteger (char value) {
    this((long) value);
  }

  /**
   * Constructs Erlang's integer number object with specific {@code byte} value.
   *
   * @param value {@code byte} number's value
   */
  public ErlangInteger (byte value) {
    this((long) (value & 0xFFL));
  }

  /**
   * Constructs Erlang's integer number object with specific {@code short} value.
   *
   * @param value {@code short} number's value
   */
  public ErlangInteger (short value) {
    this((long) value);
  }

  /**
   * Constructs Erlang's integer number object with specific {@code int} value.
   *
   * @param value {@code int} number's value
   */
  public ErlangInteger (int value) {
    this((long) value);
  }

  /**
   * Constructs Erlang's integer number object with specific {@code long} value.
   *
   * @param value {@code long} number's value
   */
  public ErlangInteger (long value) {
    super();
    this.value = BigInteger.valueOf(value);
    setupType(value);
  }

  /**
   * Constructs Erlang's integer number object with specific {@link BigInteger} value.
   *
   * @param value {@link BigInteger} number's value
   */
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
      if (cachedMagnitude == null) {
        byte[] bytes = value.abs().toByteArray();
        int index = 0;
        for (; index < bytes.length && bytes[index] == 0; index++) {
          // skip leading zeros
        }

        cachedMagnitude = Arrays.copyOfRange(bytes, index, bytes.length);
        reverse(cachedMagnitude);
      }

      int length = cachedMagnitude.length;
      if ((length & 0xFF) == length) {
        buffer.writeByte(length); // length
      } else {
        buffer.writeInt(length); // length
      }
      val sign = value.signum() < 0
                 ? 1
                 : 0;
      buffer.writeByte(sign);
      buffer.writeBytes(cachedMagnitude);
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

  @Getter
  @FieldDefaults(level = PRIVATE)
  private static class ByteArrayHashCode implements ByteProcessor {

    int hashCode = 1;

    @Override
    public boolean process (byte value) throws Exception {
      hashCode = 31 * hashCode + value;
      return true;
    }
  }
}
