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
package io.appulse.encon.java.module.connection.handshake;

import static java.lang.Integer.MAX_VALUE;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import io.appulse.encon.java.RemoteNode;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author alabazin
 */
@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@FieldDefaults(level = PRIVATE, makeFinal = true)
abstract class AbstractHandshakeChannelInitializer extends ChannelInitializer<SocketChannel> {

  private static final ChannelOutboundHandler LENGTH_FIELD_PREPENDER;

  private static final ChannelOutboundHandler ENCODER;

  static {
    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(2, false);
    ENCODER = new HandshakeEncoder();
  }

  static RemoteNode cleanup (@NonNull ChannelPipeline pipeline) {
    val handshakeHandler = (AbstractHandshakeHandler) pipeline.get("HANDLER");
    val remoteNode = handshakeHandler.getRemoteNode();

    pipeline.remove("READ_TIMEOUT");
    pipeline.remove("LENGTH_PREPENDER");
    pipeline.remove("LENGTH_DECODER");
    pipeline.remove("DECODER");
    pipeline.remove("ENCODER");
    pipeline.remove("HANDLER");

    log.debug("Handshake pipeline was removed");
    return remoteNode;
  }

  @NonNull
  ChannelInboundHandler decoder;

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    val handler = createHandler();
    socketChannel.pipeline()
        .addLast("READ_TIMEOUT", new ReadTimeoutHandler(5))
        .addLast("LENGTH_PREPENDER", LENGTH_FIELD_PREPENDER)
        .addLast("LENGTH_DECODER", new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 2))
        .addLast("DECODER", decoder)
        .addLast("ENCODER", ENCODER)
        .addLast("HANDLER", handler);

    log.debug("Handshake pipeline initialized");
  }

  protected abstract AbstractHandshakeHandler createHandler ();
}
