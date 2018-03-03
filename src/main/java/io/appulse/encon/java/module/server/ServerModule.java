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
import io.appulse.encon.java.module.connection.Connection;
import io.appulse.encon.java.module.connection.Pipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.CompletableFuture;

import lombok.NonNull;
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

  @NonNull
  NodeInternalApi internal;

  EventLoopGroup bossGroup = new NioEventLoopGroup(1);

  EventLoopGroup workerGroup = new NioEventLoopGroup(2);

  @NonFinal
  volatile boolean closed;

  @SneakyThrows
  public void start (int port) {
    log.debug("Starting server on port {}", port);
    new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {

          @Override
          public void initChannel (SocketChannel channel) throws Exception {
            CompletableFuture<Connection> future = new CompletableFuture<>();

            Pipeline.builder()
                .internal(internal)
                .future(future)
                .build()
                .setupServerHandshake(channel.pipeline());

            internal.connections().addConnection(future);
          }
        })
        .option(SO_BACKLOG, 128)
        .childOption(SO_KEEPALIVE, true)
        .childOption(TCP_NODELAY, true)
        .bind(port);
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
