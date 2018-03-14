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

package io.appulse.encon.java.module.connection.regular;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;

import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.utils.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
public class ClientDecoder extends ReplayingDecoder<Message> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf buffer, List<Object> out) throws Exception {
    log.debug("Decoding...");
    val messageSize = buffer.readShort();
    log.debug("Decoding message size: {}", messageSize);
    if (messageSize == 0) {
      return;
    }

    ByteBuf buf = buffer.readBytes(messageSize);
    val messageBytes = new byte[messageSize];
    buf.getBytes(0, messageBytes);

    Bytes bytes = Bytes.wrap(messageBytes);
    log.debug("Pass through: {}", bytes.getByte() == 112);

    ErlangTerm header = readTerm(bytes);
    log.debug("Received header:\n{}\n", header);

    ControlMessage controlMessage = ControlMessage.parse(header);
    log.debug("Received control message:\n{}\n", controlMessage);

    Optional<ErlangTerm> optionalBody = empty();
    if (bytes.remaining() > 0) {
      ErlangTerm body = readTerm(bytes);
      log.debug("Received body:\n{}\n", body);
      optionalBody = ofNullable(body);
    }

    out.add(new Message(controlMessage, optionalBody));
  }

  private ErlangTerm readTerm (Bytes bytes) {
    log.debug("Version byte: {}", bytes.getUnsignedByte());
    return ErlangTerm.newInstance(bytes);
  }
}
