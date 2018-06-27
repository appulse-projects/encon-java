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

import static io.netty.handler.logging.LogLevel.DEBUG;
import static java.lang.Integer.MAX_VALUE;

import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public final class RegularPipeline {

  private static final ChannelDuplexHandler LOGGING_HANDLER;

  private static final LengthFieldPrepender LENGTH_FIELD_PREPENDER;

  private static final ChannelInboundHandler TICK_TOCK_HANDLER;

  private static final ChannelOutboundHandler MESSAGE_ENCODER;

  private static final ChannelInboundHandler MESSAGE_DECODER;

  static {
    LOGGING_HANDLER = new LoggingHandler(DEBUG);
    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(4, false);
    TICK_TOCK_HANDLER = new TickTockHandler();
    MESSAGE_ENCODER = new MessageEncoder();
    MESSAGE_DECODER = new MessageDecoder();
  }

  public static ConnectionHandler setup (@NonNull ChannelPipeline pipeline,
                                         @NonNull Node node,
                                         @NonNull RemoteNode remoteNode,
                                         @NonNull Consumer<RemoteNode> channelCloseAction
  ) {
    val handler = new ConnectionHandler(node, remoteNode, channelCloseAction);

    pipeline
        .addLast(LOGGING_HANDLER)
        .addLast(TICK_TOCK_HANDLER)
        .addLast(LENGTH_FIELD_PREPENDER)
        .addLast(new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 4))
        .addLast(MESSAGE_ENCODER)
        .addLast(MESSAGE_DECODER)
        .addLast(handler);

    return handler;
  }

  private RegularPipeline () {
  }
}
