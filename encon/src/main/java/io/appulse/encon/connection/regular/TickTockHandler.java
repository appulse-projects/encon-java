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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@Sharable
public class TickTockHandler extends ChannelInboundHandlerAdapter {

  private static final ByteBuf TICK_TOCK = Unpooled.wrappedBuffer(new byte[] { 0, 0, 0, 0 });

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object message) throws Exception {
    val buffer = (ByteBuf) message;

    if (buffer.readableBytes() >= TICK_TOCK.maxCapacity() && buffer.getInt(0) == 0) {
      log.debug("TICK-TOCK message detected, sending response");
      TICK_TOCK.retain();
      context.writeAndFlush(TICK_TOCK.duplicate());
      if (buffer.readableBytes() == TICK_TOCK.maxCapacity()) {
        log.debug("There is no more bytes in message, stop pipelining");
        return;
      }
      buffer.readerIndex(TICK_TOCK.maxCapacity());
    }

    context.fireChannelRead(buffer);
  }
}
