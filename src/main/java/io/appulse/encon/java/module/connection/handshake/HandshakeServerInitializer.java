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

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Connection;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author alabazin
 */
@FieldDefaults(level = PRIVATE)
public final class HandshakeServerInitializer extends AbstractHandshakeChannelInitializer {

  private static final ChannelInboundHandler DECODER;

  static {
    DECODER = new HandshakeDecoder(false);
  }

  final NodeInternalApi internal;

  CompletableFuture<Connection> future;

  @Builder
  public HandshakeServerInitializer (@NonNull NodeInternalApi internal) {
    super(DECODER);
    this.internal = internal;
  }

  @Override
  protected void initChannel (SocketChannel socketChannel) throws Exception {
    this.future = new CompletableFuture<>();
    super.initChannel(socketChannel);
    internal.connections().addConnection(future);
  }

  @Override
  protected AbstractHandshakeHandler createHandler () {
    return new HandshakeHandlerServer(internal, future);
  }
}
