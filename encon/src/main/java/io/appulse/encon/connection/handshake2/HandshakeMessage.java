/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.connection.handshake2;

import static lombok.AccessLevel.PRIVATE;

import java.util.stream.Stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class HandshakeMessage {

  @SuppressWarnings("unchecked")
  public static <T extends HandshakeMessage> T from (@NonNull ByteBuf buffer, boolean isClient) {
    val tagByte = buffer.readByte();

    val tag = Tag.from(tagByte, isClient);
    log.debug("Parsing a handshake message with tag {}({})", tag, tagByte);

    switch (tag) {
    case NAME_REQUEST:
      return (T) new HandshakeMessageNameRequest(buffer);
    case STATUS_RESPONSE:
      return (T) new HandshakeMessageStatusResponse(buffer);
    case CHALLENGE_REQUEST:
      return (T) new HandshakeMessageChallengeRequest(buffer);
    case CHALLENGE_RESPONSE:
      return (T) new HandshakeMessageChallengeResponse(buffer);
    case CHALLENGE_ACKNOWLEDGE:
      return (T) new HandshakeMessageChallengeAcknowledge(buffer);
    case UNKNOWN:
    default:
      log.error("Unknown handshake message tag {}({})", tag, tagByte);
      throw new HandshakeExceptionParsingError(tagByte);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends HandshakeMessage> T from (@NonNull ByteBuf buffer, @NonNull Tag tag) {
    switch (tag) {
    case NAME_REQUEST:
      return (T) new HandshakeMessageNameRequest(buffer);
    case STATUS_RESPONSE:
      return (T) new HandshakeMessageStatusResponse(buffer);
    case CHALLENGE_REQUEST:
      return (T) new HandshakeMessageChallengeRequest(buffer);
    case CHALLENGE_RESPONSE:
      return (T) new HandshakeMessageChallengeResponse(buffer);
    case CHALLENGE_ACKNOWLEDGE:
      return (T) new HandshakeMessageChallengeAcknowledge(buffer);
    case UNKNOWN:
    default:
      log.error("Unknown handshake message tag {}({})", tag, tag.getCode());
      throw new HandshakeExceptionParsingError(tag.getCode());
    }
  }

  protected byte[] readAllRestBytes (ByteBuf buffer) {
    int length = buffer.readableBytes();
    byte[] result = new byte[length];
    buffer.readBytes(result);
    return result;
  }

  Tag tag;

  public final ByteBuf toByteBuf () {
    val buffer = Unpooled.buffer(4, Integer.MAX_VALUE);
    val result = Unpooled.unreleasableBuffer(buffer);
    return writeTo(result);
  }

  public ByteBuf writeTo (ByteBuf buffer) {
    val position = buffer.writerIndex();
    buffer
        .writeShort(0)
        .writeByte(tag.getCode());

    write(buffer);
    buffer.setShort(position, buffer.writerIndex() - position - Short.BYTES);
    return buffer;
  }

  abstract void write (ByteBuf buffer);

  abstract void read (ByteBuf buffer);

  @Getter
  @AllArgsConstructor
  public enum Tag {

    NAME_REQUEST((byte) 23),          // 1 0111 = 'n'
    STATUS_RESPONSE((byte) 28),       // 1 1100 = 's'
    CHALLENGE_REQUEST((byte) 23),     // 1 0111 = 'n'
    CHALLENGE_RESPONSE((byte) 27),    // 1 1011 = 'r'
    CHALLENGE_ACKNOWLEDGE((byte) 10), //   1010 = 'a'
    UNKNOWN((byte) 0);

    byte code;

    static Tag from (byte code, boolean isClient) {
      return Stream.of(values())
          .filter(it -> (isClient && it != NAME_REQUEST) || (!isClient && it != CHALLENGE_REQUEST))
          .filter(it -> it.getCode() == code)
          .findAny()
          .orElse(UNKNOWN);
    }
  }
}
