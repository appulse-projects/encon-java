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

package io.appulse.encon.connection.handshake.message;

import static lombok.AccessLevel.PRIVATE;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class Message {

  @SneakyThrows
  public static <T extends Message> T parse (@NonNull ByteBuf buffer, @NonNull Class<T> type) {
    if (!MessageType.check(buffer.readByte(), type)) {
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

  public final void writeTo (ByteBuf buffer) {
    buffer.writeByte(type.getTag());
    write(buffer);
  }

  /**
   * Writes instance state to byte buffer.
   *
   * @param buffer byte buffer
   */
  abstract void write (ByteBuf buffer);

  /**
   * Reads message's fields values from byte buffer.
   *
   * @param buffer byte buffer
   */
  abstract void read (ByteBuf buffer);

  protected final byte[] readAllRestBytes (ByteBuf buffer) {
    int length = buffer.readableBytes();
    byte[] result = new byte[length];
    buffer.readBytes(result);
    return result;
  }
}
