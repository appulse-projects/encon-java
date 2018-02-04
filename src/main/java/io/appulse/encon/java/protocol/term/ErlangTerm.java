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

package io.appulse.encon.java.protocol.term;

import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import io.appulse.encon.java.protocol.TermType;
import io.appulse.utils.Bytes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class ErlangTerm implements IntegralNumberTerm,
                                            FloatingPointNumberTerm,
                                            BooleanTerm,
                                            StringTerm,
                                            ContainerTerm,
                                            SpecificTerm {

  public static <T extends ErlangTerm> T newInstance (@NonNull ByteBuffer byteBuffer) {
    val buffer = Bytes.wrap(byteBuffer);
    return newInstance(buffer);
  }

  public static <T extends ErlangTerm> T newInstance (@NonNull byte[] bytes) {
    val buffer = Bytes.wrap(bytes);
    return newInstance(buffer);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public static <T extends ErlangTerm> T newInstance (@NonNull Bytes buffer) {
    val type = TermType.of(buffer.getByte());
    Class<T> klass = (Class<T>) type.getType();
    Constructor<T> constructor = klass.getConstructor(TermType.class);
    T result = (T) constructor.newInstance(type);
    result.read(buffer);
    return result;
  }

  TermType type;

  protected ErlangTerm (@NonNull TermType type) {
    this.type = type;
  }

  public final byte[] toBytes () {
    val buffer = Bytes.allocate();
    writeTo(buffer);
    return buffer.array();
  }

  public final void writeTo (@NonNull Bytes buffer) {
    buffer.put1B(type.getCode());
    write(buffer);
  }

  protected abstract void read (Bytes buffer);

  protected abstract void write (Bytes buffer);
}
