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

import static java.lang.Integer.MAX_VALUE;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.config.CompressionConfig;
import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.AbstractHandshakeHandler;
import io.appulse.encon.java.module.connection.handshake.HandshakeDecoder;
import io.appulse.encon.java.module.connection.handshake.HandshakeEncoder;
import io.appulse.encon.java.module.connection.handshake.HandshakeHandlerClient;
import io.appulse.encon.java.module.connection.handshake.HandshakeHandlerServer;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;
import io.appulse.encon.java.module.connection.regular.CompressionDecoder;
import io.appulse.encon.java.module.connection.regular.CompressionEncoder;
import io.appulse.encon.java.module.connection.regular.MessageDecoder;
import io.appulse.encon.java.module.connection.regular.MessageEncoder;
import io.appulse.encon.java.module.connection.regular.TickTockHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Pipeline {

  private static final ChannelOutboundHandler HANDSHAKE_LENGTH_FIELD_PREPENDER;

  private static final ChannelOutboundHandler HANDSHAKE_ENCODER;

  private static final ChannelInboundHandler HANDSHAKE_DECODER_CLIENT;

  private static final ChannelInboundHandler HANDSHAKE_DECODER_SERVER;

  private static final ChannelOutboundHandler LENGTH_FIELD_PREPENDER;

  private static final ChannelInboundHandler TICK_TOCK_HANDLER;

  private static ChannelOutboundHandler COMPRESSION_ENCODER;

  private static final ChannelInboundHandler COMPRESSION_DECODER;

  private static final ChannelOutboundHandler MESSAGE_ENCODER;

  private static final ChannelInboundHandler MESSAGE_DECODER;

  private static final String TIMEOUT_HANDLER_NAME;

  private static final String DECODER_NAME;

  private static final String ENCODER_NAME;

  private static final String HANDLER_NAME;

  static {
    HANDSHAKE_LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(2, false);
    HANDSHAKE_ENCODER = new HandshakeEncoder();
    HANDSHAKE_DECODER_CLIENT = new HandshakeDecoder(true);
    HANDSHAKE_DECODER_SERVER = new HandshakeDecoder(false);

    LENGTH_FIELD_PREPENDER = new LengthFieldPrepender(4, false);
    TICK_TOCK_HANDLER = new TickTockHandler();
    COMPRESSION_DECODER = new CompressionDecoder();
    MESSAGE_ENCODER = new MessageEncoder();
    MESSAGE_DECODER = new MessageDecoder();

    TIMEOUT_HANDLER_NAME = "readTimeoutHandler";
    DECODER_NAME = "decoder";
    ENCODER_NAME = "encoder";
    HANDLER_NAME = "handler";
  }

  NodeInternalApi internal;

  CompletableFuture<Connection> future;

  @Builder
  public Pipeline (@NonNull NodeInternalApi internal, @NonNull CompletableFuture<Connection> future) {
    this.internal = internal;
    this.future = future;
    if (COMPRESSION_ENCODER == null) {
      ofNullable(internal)
          .map(NodeInternalApi::config)
          .filter(Objects::nonNull)
          .map(NodeConfig::getCompression)
          .filter(Objects::nonNull)
          .filter(CompressionConfig::getEnabled)
          .ifPresent(it -> {
            COMPRESSION_ENCODER = new CompressionEncoder(it.getLevel());
          });
    }
  }

  public void setupClientHandshake (@NonNull ChannelPipeline pipeline, @NonNull RemoteNode remote) {
    pipeline
        .addLast(TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(5))
        .addLast("1", HANDSHAKE_LENGTH_FIELD_PREPENDER)
        .addLast("2", new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 2))
        .addLast(DECODER_NAME, HANDSHAKE_DECODER_CLIENT)
        .addLast(ENCODER_NAME, HANDSHAKE_ENCODER)
        .addLast(HANDLER_NAME, new HandshakeHandlerClient(this, internal, remote));

    log.debug("Client handshake pipline was setted up for {}", remote);
  }

  public void setupServerHandshake (@NonNull ChannelPipeline pipeline) {
    pipeline
        .addLast(TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(5))
        .addLast("1", HANDSHAKE_LENGTH_FIELD_PREPENDER)
        .addLast("2", new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 2))
        .addLast(DECODER_NAME, HANDSHAKE_DECODER_SERVER)
        .addLast(ENCODER_NAME, HANDSHAKE_ENCODER)
        .addLast(HANDLER_NAME, new HandshakeHandlerServer(this, internal));

    log.debug("Server handshake pipline was setted up for {}", pipeline.channel().remoteAddress());
  }

  public void setupPipeline (@NonNull ChannelPipeline pipeline) {
    val handshakeHandler = (AbstractHandshakeHandler) pipeline.get("handler");
    val remote = handshakeHandler.getRemoteNode();
    val handler = new ClientRegularHandler(internal, remote, future);

    pipeline.remove(TIMEOUT_HANDLER_NAME);
    pipeline.remove("1");
    pipeline.remove("2");
    pipeline.remove(DECODER_NAME);
    pipeline.remove(ENCODER_NAME);
    pipeline.remove(HANDLER_NAME);

    pipeline.addLast(LENGTH_FIELD_PREPENDER);

    pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_VALUE, 0, 4));

    pipeline.addLast(TICK_TOCK_HANDLER);
//    pipeline.addLast(COMPRESSION_DECODER);
//    ofNullable(COMPRESSION_ENCODER)
//        .ifPresent(pipeline::addLast);
    pipeline.addLast(MESSAGE_ENCODER);
    pipeline.addLast(MESSAGE_DECODER);
    pipeline.addLast(handler);

    val connection = new Connection(remote, handler);
    future.complete(connection);

    log.debug("Connection for {} was added to pool", remote);
  }

  public void exception (Throwable ex) {
    future.completeExceptionally(ex);
  }
}
