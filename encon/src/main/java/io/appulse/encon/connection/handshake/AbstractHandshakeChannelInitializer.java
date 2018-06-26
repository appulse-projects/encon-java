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

import static io.netty.handler.logging.LogLevel.DEBUG;
import static java.lang.Integer.MAX_VALUE;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@FieldDefaults(level = PRIVATE, makeFinal = true)
abstract class AbstractHandshakeChannelInitializer extends ChannelInitializer<SocketChannel> {

  private static final ChannelDuplexHandler LOGGING_HANDLER;

  private static final ChannelOutboundHandler LENGTH_FIELD_PREPENDER;

  private static final ChannelOutboundHandler ENCODER;

  static {
    LOGGING_HANDLER = new LoggingHandler(DEBUG);
    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(2, false);
    ENCODER = new HandshakeEncoder();
  }

  static void cleanup (@NonNull ChannelPipeline pipeline) {
    pipeline.remove("LOGGING");
    pipeline.remove("READ_TIMEOUT");
    pipeline.remove("LENGTH_PREPENDER");
    pipeline.remove("LENGTH_DECODER");
    pipeline.remove("DECODER");
    pipeline.remove("ENCODER");
    pipeline.remove("HANDLER");

    log.debug("Handshake pipeline for {} was removed",
              pipeline.channel().remoteAddress());
  }

  @NonNull
  ChannelInboundHandler decoder;

  @Override
  protected void initChannel (SocketChannel socketChannel) throws Exception {
    throw new UnsupportedOperationException();
  }

  protected void initChannel (SocketChannel socketChannel, AbstractHandshakeHandler handler) {
    socketChannel.pipeline()
        .addLast("LOGGING", LOGGING_HANDLER)
        .addLast("READ_TIMEOUT", new ReadTimeoutHandler(5))
        .addLast("LENGTH_PREPENDER", LENGTH_FIELD_PREPENDER)
        .addLast("LENGTH_DECODER", new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 2))
        .addLast("DECODER", decoder)
        .addLast("ENCODER", ENCODER)
        .addLast("HANDLER", handler);

    log.debug("Handshake pipeline for {} was initialized",
              socketChannel.remoteAddress());
  }
}
