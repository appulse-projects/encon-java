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

package io.appulse.encon.connection.regular;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Sharable
public class MessageEncoder extends MessageToByteEncoder<Message> {

  public MessageEncoder () {
    super(false);
  }

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void encode (ChannelHandlerContext context, Message message, ByteBuf out) throws Exception {
    try {
      out.writeByte(0x70);
      out.writeByte(0x83);
      message.getHeader().writeTo(out);

      val body = message.getBody();
      if (body != null) {
        out.writeByte(0x83);
        body.writeTo(out);
      }
      log.debug("Message was sent");
    } catch (Exception ex) {
      log.error("Error during encoding message for {}\n  {}\n",
                context.channel().remoteAddress(), message, ex);
      throw ex;
    }
  }
}
