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

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Connection;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author alabazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE)
public final class HandshakeServerInitializer extends AbstractHandshakeChannelInitializer {

  private static final ChannelInboundHandler DECODER;

  static {
    DECODER = new HandshakeDecoder(false);
  }

  final NodeInternalApi internal;

  @Builder
  public HandshakeServerInitializer (@NonNull NodeInternalApi internal) {
    super(DECODER);
    this.internal = internal;
  }

  @Override
  protected void initChannel (SocketChannel socketChannel) throws Exception {
    log.debug("Initializing new server socket channel pipeline for {}",
              socketChannel.remoteAddress());

    CompletableFuture<Connection> future = new CompletableFuture<>();
    val handler = new HandshakeHandlerServer(internal, future);
    initChannel(socketChannel, handler);
    internal.connections().addConnection(future);
  }
}
