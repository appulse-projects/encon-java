/*
 * Copyright 2018 Appulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon.java.module.connection.handshake;

import static io.appulse.encon.java.module.connection.handshake.message.MessageType.NAME;
import static io.appulse.encon.java.module.connection.handshake.message.MessageType.UNDEFINED;

import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.MessageType;
import io.appulse.utils.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
public class ClientHandshakeDecoder extends ReplayingDecoder<Message> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) throws Exception {
    val messageSize = buffer.readShort();
    log.debug("Decoding message size: {}", messageSize);

    ByteBuf buf = buffer.readBytes(messageSize);
    val messageBytes = new byte[messageSize];
    buf.getBytes(0, messageBytes);

    val bytes = Bytes.wrap(messageBytes);

    val message = parse(bytes);

    out.add(message);
    log.debug("Decoded message {} from {}", message, context.channel().remoteAddress());
  }

  private Message parse (Bytes bytes) {
    val tag = bytes.getByte(0);
    return Stream.of(MessageType.values())
        .filter(it -> it != NAME)
        .filter(it -> it != UNDEFINED)
        .filter(it -> it.getTag() == tag)
        .findFirst()
        .map(it -> Message.parse(bytes, it.getType()))
        .orElseThrow(() -> new HandshakeException("Unknown income message with tag " + tag));
  }
}
