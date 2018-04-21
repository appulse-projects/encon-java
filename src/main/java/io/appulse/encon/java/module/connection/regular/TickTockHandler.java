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

package io.appulse.encon.java.module.connection.regular;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 08.04.2018
 */
@Slf4j
@Sharable
public class TickTockHandler extends ChannelInboundHandlerAdapter {

  private static final byte[] TICK_TOCK_MESSAGE = new byte[4];

  private static final byte[] TICK_TOCK_RESPONSE = new byte[0];

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    val message = String.format("Error during channel connection with %s",
                                context.channel().remoteAddress().toString());

    log.error(message, cause);
    context.fireExceptionCaught(cause);
    context.close();
  }

  @Override
  public void channelRead (ChannelHandlerContext context, Object msg) throws Exception {
    byte[] bytes = (byte[]) msg;
    if (Arrays.equals(bytes, TICK_TOCK_MESSAGE)) {
      context.writeAndFlush(TICK_TOCK_RESPONSE);
      return;
    }
    context.fireChannelRead(msg);
  }
}
