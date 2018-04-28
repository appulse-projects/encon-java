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

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.Connection;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author alabazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class HandshakeClientInitializer extends AbstractHandshakeChannelInitializer {

  private static final ChannelInboundHandler DECODER;

  static {
    DECODER = new HandshakeDecoder(true);
  }

  NodeInternalApi internal;

  CompletableFuture<Connection> future;

  RemoteNode remote;

  @Builder
  public HandshakeClientInitializer (@NonNull NodeInternalApi internal,
                                     @NonNull CompletableFuture<Connection> future,
                                     @NonNull RemoteNode remote
  ) {
    super(DECODER);
    this.internal = internal;
    this.future = future;
    this.remote = remote;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    val handler = new HandshakeHandlerClient(internal, future, remote);
    super.initChannel(socketChannel, handler);
  }
}
