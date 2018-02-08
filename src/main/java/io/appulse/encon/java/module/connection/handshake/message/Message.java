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

package io.appulse.encon.java.module.connection.handshake.message;

import static lombok.AccessLevel.PRIVATE;

import java.nio.ByteBuffer;

import io.appulse.utils.Bytes;
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
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class Message {

  public static <T extends Message> T parse (@NonNull ByteBuffer byteBuffer, @NonNull Class<T> type) {
    val buffer = Bytes.wrap(byteBuffer);
    return parse(buffer, type);
  }

  public static <T extends Message> T parse (@NonNull byte[] byteBuffer, @NonNull Class<T> type) {
    val buffer = Bytes.wrap(byteBuffer);
    return parse(buffer, type);
  }

  @SneakyThrows
  public static <T extends Message> T parse (@NonNull Bytes buffer, @NonNull Class<T> type) {
    if (!MessageType.check(buffer.getByte(), type)) {
      throw new IllegalArgumentException();
    }
    T result = type.newInstance();
    result.read(buffer);
    return result;
  }

  MessageType type;

  protected Message (@NonNull MessageType type) {
    this.type = type;
  }

  public byte[] toBytes () {
    val buffer = Bytes.allocate()
        .put2B(0) // reserve space for final request length
        .put1B(type.getTag());

    write(buffer);
    return buffer
        .put2B(0, buffer.limit() - Short.BYTES) // put real size
        .array();
  }

  abstract void write (Bytes buffer);

  abstract void read (Bytes buffer);
}
