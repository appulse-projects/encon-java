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

package io.appulse.encon.connection.handshake;

import static io.appulse.encon.connection.handshake.message.MessageType.CHALLENGE;
import static io.appulse.encon.connection.handshake.message.MessageType.NAME;
import static io.appulse.encon.connection.handshake.message.MessageType.UNDEFINED;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;

import io.appulse.encon.connection.handshake.exception.HandshakeException;
import io.appulse.encon.connection.handshake.message.Message;
import io.appulse.encon.connection.handshake.message.MessageType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@Sharable
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class HandshakeDecoder extends MessageToMessageDecoder<ByteBuf> {

  boolean isClient;

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) throws Exception {
    log.debug("decoding");
    buffer.readShort(); // skip size

    val tag = buffer.getByte(buffer.readerIndex());
    val message = Stream.of(MessageType.values())
        .filter(it -> it != UNDEFINED)
        .filter(it -> (isClient && it != NAME) || (!isClient && it != CHALLENGE))
        .filter(it -> it.getTag() == tag)
        .findFirst()
        .map(it -> Message.parse(buffer, it.getType()))
        .orElseThrow(() -> new HandshakeException("Unknown income message with tag " + tag));

    out.add(message);
  }
}
