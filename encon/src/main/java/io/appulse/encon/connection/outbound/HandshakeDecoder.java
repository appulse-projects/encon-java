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

package io.appulse.encon.connection.outbound;

import static io.appulse.encon.connection.handshake2.HandshakeMessage.Tag.NAME_REQUEST;
import static io.appulse.encon.connection.handshake2.HandshakeMessage.Tag.UNKNOWN;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;

import io.appulse.encon.connection.handshake2.HandshakeException;
import io.appulse.encon.connection.handshake2.HandshakeMessage;

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
 * @since 2.0.0
 * @author Artem Labazin
 */
@Slf4j
@Sharable
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class HandshakeDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) throws Exception {
    buffer.readShort(); // skip size

    val tagByte = buffer.readByte();
    val message = Stream.of(HandshakeMessage.Tag.values())
        .filter(it -> it.getCode() == tagByte)
        .filter(it -> it != NAME_REQUEST)
        .filter(it -> it != UNKNOWN)
        .findAny()
        .map(it -> HandshakeMessage.from(buffer, it))
        .orElseThrow(() -> new HandshakeException("Unknown income message with tag '" + tagByte + '\''));;

    out.add(message);
  }
}
