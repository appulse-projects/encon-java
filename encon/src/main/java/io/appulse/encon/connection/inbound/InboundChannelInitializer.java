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

import static io.netty.handler.logging.LogLevel.DEBUG;

import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.regular.ConnectionHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@Slf4j
public class InboundChannelInitializer extends ChannelInitializer<SocketChannel> {

  private static final ChannelDuplexHandler LOGGING_HANDLER;

  static {
    LOGGING_HANDLER = new LoggingHandler(DEBUG);
  }

  Node node;

  Consumer<RemoteNode> closeAction;

  @Override
  protected void initChannel (SocketChannel socketChannel) throws Exception {
    ChannelPipeline pipeline = socketChannel.pipeline();
    if (log.isDebugEnabled()) {
      pipeline.addLast(LOGGING_HANDLER);
    }

    val handshakeHandler = HandshakeHandler.builder()
        .node(node)
        .onSuccess(remoteNode -> {
          val handler = ConnectionHandler.builder()
              .node(node)
              .remote(remoteNode)
              .channelCloseAction(closeAction)
              .build();

          pipeline.addLast("HANDLER", handler);

          log.debug("Connection to {}, handshake was successful", remoteNode);
        })
        .build();

    HandshakePipeline.init(pipeline, handshakeHandler);
    log.debug("Handshake pipeline for {} was initialized",
              socketChannel.remoteAddress());
  }
}
