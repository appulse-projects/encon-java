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

package io.appulse.encon.module.connection.regular;

import java.util.List;

import io.appulse.encon.module.connection.control.ControlMessage;
import io.appulse.encon.terms.ErlangTerm;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Sharable
public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void decode (ChannelHandlerContext context, ByteBuf msg, List<Object> out) throws Exception {
    log.debug("Decoding a new message");
    msg.readInt(); // skip size
    msg.readByte(); // skip pass through

    val header = readTerm(msg);
    val controlMessage = ControlMessage.parse(header);

    ErlangTerm body = null;
    if (msg.readerIndex() < msg.capacity()) {
      body = readTerm(msg);
    }

    out.add(new Message(controlMessage, body));
  }

  private ErlangTerm readTerm (ByteBuf buffer) {
    buffer.readUnsignedByte(); // skip version byte;
    return ErlangTerm.newInstance(buffer);
  }
}
