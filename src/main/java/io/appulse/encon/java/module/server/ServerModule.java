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

package io.appulse.encon.java.module.server;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.connection.handshake.ClientHandshakeHandler;
import io.appulse.encon.java.module.connection.handshake.HandshakeDecoder;
import io.appulse.encon.java.module.connection.handshake.HandshakeEncoder;
import io.appulse.encon.java.module.connection.regular.ClientDecoder;
import io.appulse.encon.java.module.connection.regular.ClientEncoder;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ServerModule implements ServerModuleApi, Closeable {

  private static final ChannelOutboundHandler HANDSHAKE_ENCODER;

  private static final ChannelOutboundHandler REGULAR_ENCODER;

  static {
    HANDSHAKE_ENCODER = new HandshakeEncoder();

    REGULAR_ENCODER = new ClientEncoder();
  }

  NodeInternalApi internal;

  EventLoopGroup bossGroup = new NioEventLoopGroup(1);

  EventLoopGroup workerGroup = new NioEventLoopGroup(2);

  @NonFinal
  ClientRegularHandler handler;

  @NonFinal
  volatile boolean closed;

  @SneakyThrows
  public void start (int port) {
    log.debug("Starting server on port {}", port);
    handler = new ClientRegularHandler(internal);
    // try {
      new ServerBootstrap()
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel (SocketChannel channel) throws Exception {
              channel.pipeline()
                  .addLast("decoder", new HandshakeDecoder(false))
                  .addLast("encoder", HANDSHAKE_ENCODER)
                  .addLast("handler", HandshakeHandler.builder()
                          .internal(internal)
                          .build());
            }
          })
          .option(SO_BACKLOG, 128)
          .childOption(SO_KEEPALIVE, true)
          .childOption(TCP_NODELAY, true)
          .bind(port);
          // .sync()
          // // Wait until the server socket is closed.
          // .channel().closeFuture().sync();
    // } catch (InterruptedException ex) {
    //   log.error("Server work exception", ex);
    // } finally {
    //   close();
    // }
  }

  @Override
  @SneakyThrows
  public void close () {
    if (closed) {
      log.debug("Server was already closed");
      return;
    }
    closed = true;

    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
    log.debug("Boss and workers groups are closed");

    log.info("Server was closed");
  }
}
