/*
 * Copyright 2019 the original author or authors.
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

package io.appulse.encon.connection.inbound;

import static java.lang.Integer.MAX_VALUE;

import java.util.stream.Stream;

import io.appulse.encon.connection.handshake2.HandshakeEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
class HandshakePipeline {

  private static final ChannelOutboundHandler LENGTH_FIELD_PREPENDER;

  private static final ChannelOutboundHandler ENCODER;

  static {
    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(2, false);
    ENCODER = new HandshakeEncoder();
  }

  static void init (ChannelPipeline pipeline, ChannelHandler handler) throws Exception {
    pipeline
        .addLast("READ_TIMEOUT", new ReadTimeoutHandler(5))
        .addLast("LENGTH_PREPENDER", LENGTH_FIELD_PREPENDER)
        .addLast("LENGTH_DECODER", new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 2))
        .addLast("DECODER", new HandshakeDecoder())
        .addLast("ENCODER", ENCODER)
        .addLast("HANDLER", handler);
  }

  static void clean (ChannelPipeline pipeline) {
    Stream.of(
      "READ_TIMEOUT",
      "LENGTH_PREPENDER",
      "LENGTH_DECODER",
      "DECODER",
      "ENCODER",
      "HANDLER"
    ).forEach(pipeline::remove);
  }

  private HandshakePipeline () {
    throw new UnsupportedOperationException();
  }
}
