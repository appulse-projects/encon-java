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

import static io.netty.handler.logging.LogLevel.DEBUG;
import static java.lang.Integer.MAX_VALUE;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
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
 * @author alabazin
 */
public class RegularPipeline {

  private static final ChannelDuplexHandler LOGGING_HANDLER;

  private static final LengthFieldPrepender LENGTH_FIELD_PREPENDER;

  private static final ChannelInboundHandler TICK_TOCK_HANDLER;

//  private static ChannelOutboundHandler COMPRESSION_ENCODER;
//
//  private static final ChannelInboundHandler COMPRESSION_DECODER;
  private static final ChannelOutboundHandler MESSAGE_ENCODER;

  private static final ChannelInboundHandler MESSAGE_DECODER;

  static {
    LOGGING_HANDLER = new LoggingHandler(DEBUG);
    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(4, false);
    TICK_TOCK_HANDLER = new TickTockHandler();
//    COMPRESSION_DECODER = new CompressionDecoder();
    MESSAGE_ENCODER = new MessageEncoder();
    MESSAGE_DECODER = new MessageDecoder();
  }

  public static ClientRegularHandler setup (@NonNull ChannelPipeline pipeline,
                                            @NonNull NodeInternalApi internal,
                                            @NonNull RemoteNode remoteNode
  ) {
    val handler = new ClientRegularHandler(internal, remoteNode);

    pipeline
        .addLast(LOGGING_HANDLER)
        .addLast(LENGTH_FIELD_PREPENDER)
        .addLast(new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 4))
        .addLast(TICK_TOCK_HANDLER);
//    pipeline.addLast(COMPRESSION_DECODER);
//    ofNullable(COMPRESSION_ENCODER)
//        .ifPresent(pipeline::addLast);
    pipeline
        .addLast(MESSAGE_ENCODER)
        .addLast(MESSAGE_DECODER)
        .addLast(handler);

    return handler;
  }
}
