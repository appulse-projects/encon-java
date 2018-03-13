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

import io.appulse.utils.Bytes;

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
public class ClientEncoder extends MessageToByteEncoder<Message> {

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  protected void encode (ChannelHandlerContext context, Message container, ByteBuf out) throws Exception {
    log.debug("Encoding message {} for {}", container, context.channel().remoteAddress());

    try {
      val bytes = Bytes.allocate()
          .put4B(0)
          .put1B(0x70) // 112
          .put1B(0x83) // 131
          // .put(EMPTY_DISTRIBUTION_HEADER)
          .put(container.getHeader().toBytes());

      container.getBody().ifPresent(it -> {
        bytes
          .put1B(0x83) // 131
          .put(it.toBytes());
      });

      val length = bytes.limit() - Integer.BYTES;
      log.debug("Outgoing message length is: {}", length);

      val array = bytes.put4B(0, length).array();
      log.debug("Output array:\n{}", array);

      out.writeBytes(array);
      log.debug("Message was sent");
    } catch (Throwable ex) {
      log.error("Error during encoding message {} for {}", container, context.channel().remoteAddress());
      throw ex;
    }
  }
}
