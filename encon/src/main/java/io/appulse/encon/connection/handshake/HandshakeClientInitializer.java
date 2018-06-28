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

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.appulse.encon.Node;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class HandshakeClientInitializer extends AbstractHandshakeChannelInitializer {

  private static final ChannelInboundHandler DECODER;

  static {
    DECODER = new HandshakeDecoder(true);
  }

  Node node;

  CompletableFuture<Connection> future;

  RemoteNode remote;

  Consumer<RemoteNode> channelCloseAction;

  @Builder
  public HandshakeClientInitializer (@NonNull Node node,
                                     @NonNull CompletableFuture<Connection> future,
                                     @NonNull RemoteNode remote,
                                     @NonNull Consumer<RemoteNode> channelCloseAction
  ) {
    super(DECODER);
    this.node = node;
    this.future = future;
    this.remote = remote;
    this.channelCloseAction = channelCloseAction;
  }

  @Override
  protected void initChannel (SocketChannel socketChannel) throws Exception {
    val handler = new HandshakeHandlerClient(node, future, remote, channelCloseAction);
    initChannel(socketChannel, handler);
  }
}
