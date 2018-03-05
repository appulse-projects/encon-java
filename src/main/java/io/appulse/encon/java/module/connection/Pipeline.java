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

package io.appulse.encon.java.module.connection;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.AbstractHandshakeHandler;
import io.appulse.encon.java.module.connection.handshake.HandshakeDecoder;
import io.appulse.encon.java.module.connection.handshake.HandshakeEncoder;
import io.appulse.encon.java.module.connection.handshake.HandshakeHandlerClient;
import io.appulse.encon.java.module.connection.handshake.HandshakeHandlerServer;
import io.appulse.encon.java.module.connection.regular.ClientDecoder;
import io.appulse.encon.java.module.connection.regular.ClientEncoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;

import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 20.02.2018
 */
@Slf4j
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Pipeline {

  private static final ChannelOutboundHandler HANDSHAKE_ENCODER;

  private static final ChannelOutboundHandler CLIENT_ENCODER;

  static {
    HANDSHAKE_ENCODER = new HandshakeEncoder();
    CLIENT_ENCODER = new ClientEncoder();
  }

  @NonNull
  NodeInternalApi internal;

  @NonNull
  CompletableFuture<Connection> future;

  public void setupClientHandshake (@NonNull ChannelPipeline pipeline, @NonNull RemoteNode remote) {
    pipeline
        .addLast("readTimeoutHandler", new ReadTimeoutHandler(5))
        .addLast("decoder", new HandshakeDecoder(true))
        .addLast("encoder", HANDSHAKE_ENCODER)
        .addLast("handler", new HandshakeHandlerClient(this, internal, remote));

    log.debug("Client handshake pipline was setted up for {}", remote);
  }

  public void setupServerHandshake (@NonNull ChannelPipeline pipeline) {
    pipeline
        .addLast("readTimeoutHandler", new ReadTimeoutHandler(5))
        .addLast("decoder", new HandshakeDecoder(false))
        .addLast("encoder", HANDSHAKE_ENCODER)
        .addLast("handler", new HandshakeHandlerServer(this, internal));

    log.debug("Server handshake pipline was setted up for {}", pipeline.channel().remoteAddress());
  }

  public void setupPipeline (@NonNull ChannelPipeline pipeline) {
    val handshakeHandler = (AbstractHandshakeHandler) pipeline.get("handler");
    val remote = handshakeHandler.getRemoteNode();
    val handler = new ClientRegularHandler(internal, remote, future);

    pipeline.replace("decoder", "decoder", new ClientDecoder());
    pipeline.replace("encoder", "encoder", CLIENT_ENCODER);
    pipeline.replace("handler", "handler", handler);

    val connection = new Connection(remote, handler);
    future.complete(connection);

    log.debug("Connection for {} was added to pool", remote);
  }

  public void exception (Throwable ex) {
    future.completeExceptionally(ex);
  }
}
