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
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Pipeline {

  private static final ChannelOutboundHandler HANDSHAKE_ENCODER;

  private static final ChannelOutboundHandler CLIENT_ENCODER;

  private static final String TIMEOUT_HANDLER_NAME;
  private static final String DECODER_NAME;
  private static final String ENCODER_NAME;
  private static final String HANDLER_NAME;

  static {
    HANDSHAKE_ENCODER = new HandshakeEncoder();
    CLIENT_ENCODER = new ClientEncoder();

    TIMEOUT_HANDLER_NAME = "readTimeoutHandler";
    DECODER_NAME = "decoder";
    ENCODER_NAME = "encoder";
    HANDLER_NAME = "handler";
  }

  @NonNull
  NodeInternalApi internal;

  @NonNull
  CompletableFuture<Connection> future;

  public void setupClientHandshake (@NonNull ChannelPipeline pipeline, @NonNull RemoteNode remote) {
    pipeline
        .addLast(TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(5))
        .addLast(DECODER_NAME, new HandshakeDecoder(true))
        .addLast(ENCODER_NAME, HANDSHAKE_ENCODER)
        .addLast(HANDLER_NAME, new HandshakeHandlerClient(this, internal, remote));

    log.debug("Client handshake pipline was setted up for {}", remote);
  }

  public void setupServerHandshake (@NonNull ChannelPipeline pipeline) {
    pipeline
        .addLast(TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(5))
        .addLast(DECODER_NAME, new HandshakeDecoder(false))
        .addLast(ENCODER_NAME, HANDSHAKE_ENCODER)
        .addLast(HANDLER_NAME, new HandshakeHandlerServer(this, internal));

    log.debug("Server handshake pipline was setted up for {}", pipeline.channel().remoteAddress());
  }

  public void setupPipeline (@NonNull ChannelPipeline pipeline) {
    val handshakeHandler = (AbstractHandshakeHandler) pipeline.get("handler");
    val remote = handshakeHandler.getRemoteNode();
    val handler = new ClientRegularHandler(internal, remote, future);

    pipeline.remove(TIMEOUT_HANDLER_NAME);
    pipeline.replace(DECODER_NAME, DECODER_NAME, new ClientDecoder());
    pipeline.replace(ENCODER_NAME, ENCODER_NAME, CLIENT_ENCODER);
    pipeline.replace(HANDLER_NAME, HANDLER_NAME, handler);

    val connection = new Connection(remote, handler);
    future.complete(connection);

    log.debug("Connection for {} was added to pool", remote);
  }

  public void exception (Throwable ex) {
    future.completeExceptionally(ex);
  }
}
