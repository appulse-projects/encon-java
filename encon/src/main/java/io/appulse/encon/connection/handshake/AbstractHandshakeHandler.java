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

import static lombok.AccessLevel.PROTECTED;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.connection.regular.RegularPipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor(access = PROTECTED)
@FieldDefaults(level = PROTECTED, makeFinal = true)
abstract class AbstractHandshakeHandler extends ChannelInboundHandlerAdapter {

  @NonNull
  Node node;

  @NonNull
  CompletableFuture<Connection> future;

  @NonNull
  Consumer<RemoteNode> channelCloseAction;

  @NonFinal
  RemoteNode remote;

  @Override
  public void exceptionCaught (ChannelHandlerContext context, Throwable cause) throws Exception {
    log.error("Error during channel connection with {}",
              context.channel().remoteAddress(), cause);

    context.fireExceptionCaught(cause);
    context.close();
    future.completeExceptionally(cause);
  }

  /**
   * Replaces handshake pipeline with regular pipeline after successful handshake process.
   *
   * @param context connection context
   */
  protected void successHandshake (@NonNull Channel channel) {
    val pipeline = channel.pipeline();
    log.debug("Replacing pipline to regular for {}", channel.remoteAddress());

    AbstractHandshakeChannelInitializer.cleanup(pipeline);
    val handler = RegularPipeline.setup(pipeline, node, remote, channelCloseAction);

    future.complete(new Connection(remote, handler));

    log.debug("Connection was added to pool for\n  {}\n", remote);
  }
}
